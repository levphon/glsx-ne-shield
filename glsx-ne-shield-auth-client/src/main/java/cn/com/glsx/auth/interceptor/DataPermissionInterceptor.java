package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.model.RequireDataPermissions;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;

/**
 * 处理针对某一或某些特定数据操作（增、删、改、查）作数据权限校验
 * 1、获取用户角色，得到数据权限范围类型
 * 2、得到当前操作用户信息和部门信息
 * 3、根据数据创建者id，得到对应用户或部门
 * 4、比较2，3判断该数据是否有权被操作权限
 *
 * @author: taoyr
 **/
@Slf4j
@Component
@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
})
public class DataPermissionInterceptor implements Interceptor {

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object target = invocation.getTarget();

        if (target instanceof RoutingStatementHandler) {
            try {
                RoutingStatementHandler statementHandler = (RoutingStatementHandler) target;
                // MetaObject是mybatis里面提供的一个工具类，类似反射的效果
                MetaObject metaStatementHandler = MetaObject.forObject(statementHandler,
                        DEFAULT_OBJECT_FACTORY,
                        DEFAULT_OBJECT_WRAPPER_FACTORY,
                        REFLECTOR_FACTORY);

                MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");

                //没自定义注解直接按通过
                RequireDataPermissions dataPermissions = getDataPerm(mappedStatement);
                if (dataPermissions == null) {
                    return invocation.proceed();
                }

                //超级管理员不过滤
                if (ShieldContextHolder.isSuperAdmin()) {
                    return invocation.proceed();
                }

                SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
//                if (sqlCommandType == SqlCommandType.SELECT) {
//                    return invocation.proceed();
//                }

                //拼装sql(这里是关键！！！)
//                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
//                String originSql = boundSql.getSql(); //获取到当前需要被执行的SQL
//                String authSql = makeSql(originSql, dataPermissions); //进行数据权限过滤组装
//                //替换
//                MappedStatement newStatement = newMappedStatement(mappedStatement, new BoundSqlSqlSource(boundSql));
//                MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
//                msObject.setValue("sqlSource.boundSql.sql", authSql);
////                invocation.getArgs()[0] = newStatement;
//                log.debug("baseSql:{} authSql:{}", originSql, authSql);
//
//                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) boundSql.getParameterObject();
            } catch (Exception e) {
                log.error("数据权限拦截器异常", e);
                throw e;
            }
        }
        return invocation.proceed();
    }


    /**
     * 获取方法上的数据权限注解
     *
     * @param mappedStatement
     * @return
     * @throws ClassNotFoundException
     */
    private RequireDataPermissions getDataPerm(MappedStatement mappedStatement) throws ClassNotFoundException {
        RequireDataPermissions dataPermissions = null;
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        final Method[] method = Class.forName(className).getMethods();
        for (Method me : method) {
            if (me.getName().equals(methodName) && me.isAnnotationPresent(RequireDataPermissions.class)) {
                dataPermissions = me.getAnnotation(RequireDataPermissions.class);
                break;
            }
        }
        return dataPermissions;
    }

    /**
     * 核心代码： 将原SQL 进行解析并拼装 一个子查询  id in ( 数据权限过滤SQL )
     *
     * @param sql
     * @param dataAuth
     * @return
     * @throws JSQLParserException
     */
    private String makeSql(String sql, RequireDataPermissions dataAuth) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        Table fromItem = (Table) plain.getFromItem();
        //有别名用别名，无别名用表名，防止字段冲突报错
        String mainTableName = fromItem.getAlias() == null ? fromItem.getName() : fromItem.getAlias().getName();

        List<Long> creatorIds = ShieldContextHolder.getCreatorIds();

        log.info(creatorIds.toString());

        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String dataAuthSql = "";
        //构建子查询
        if (oneself.getCode().equals(rolePermissionType)) {
            dataAuthSql = mainTableName + ".created_by = " + ShieldContextHolder.getUserId();
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            dataAuthSql = mainTableName + ".created_by in (" + ShieldContextHolder.getCreatorIds() + ") ";
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            dataAuthSql = mainTableName + ".created_by in (" + ShieldContextHolder.getCreatorIds() + ") ";
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            dataAuthSql = mainTableName + ".created_by in (" + ShieldContextHolder.getCreatorIds() + ") ";
        } else if (all.getCode().equals(rolePermissionType)) {
            //do nothing
        }

        //构建子查询
        if (plain.getWhere() == null) {
            plain.setWhere(CCJSqlParserUtil.parseCondExpression(dataAuthSql));
        } else {
            plain.setWhere(new AndExpression(plain.getWhere(), CCJSqlParserUtil.parseCondExpression(dataAuthSql)));
        }
        return select.toString();
    }

    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    private class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

}