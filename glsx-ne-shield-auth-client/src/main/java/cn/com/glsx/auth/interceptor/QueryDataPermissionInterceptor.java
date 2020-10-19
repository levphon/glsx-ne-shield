package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.model.ParameterAnnotationHolder;
import cn.com.glsx.auth.model.RequireDataPermissions;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.common.utils.ReflectUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
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

/**
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
public class QueryDataPermissionInterceptor implements Interceptor {

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
                MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,
                        DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);

                MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");

                SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
                if (sqlCommandType != SqlCommandType.SELECT) {
                    return invocation.proceed();
                }

                RequireDataPermissions requireDataPermissions = getAnnontation(mappedStatement);

                String id = mappedStatement.getId();
                String className = id.substring(0, id.lastIndexOf("."));
                String methodName = id.substring(id.lastIndexOf(".") + 1);
                final Method[] methods = Class.forName(className).getMethods();
                for (Method me : methods) {
                    if (me.getName().equals(methodName) && me.isAnnotationPresent(RequireDataPermissions.class)) {
                        requireDataPermissions = me.getAnnotation(RequireDataPermissions.class);
                        break;
                    }
                }

                if (requireDataPermissions == null || StringUtils.isEmpty(requireDataPermissions.linkField())) {
                    return invocation.proceed();
                }

                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
                String originSql = boundSql.getSql();

                String linkPermitSql = assembleLinkPermitSql(originSql, requireDataPermissions.linkField(), ShieldContextHolder.getUserId()); //进行数据权限过滤组装

                //放回sql
                metaStatementHandler.setValue("delegate.boundSql.sql", linkPermitSql);

            } catch (Exception e) {
                log.error("数据权限拦截器异常", e);
                throw e;
            }
        }


        return invocation.proceed();
    }


    private String assembleLinkPermitSql(String sql, String accessField, Long userId) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));

        SelectBody selectBody = select.getSelectBody();

        PlainSelect plain = (PlainSelect) selectBody;

        FromItem fromItem = plain.getFromItem();

        //有别名用别名，无别名用表名，防止字段冲突报错
        Table table = (Table) fromItem;
        String mainTableName = table.getAlias() == null ? table.getName() : table.getAlias().getName();

        String permitTable = mainTableName + "_permit";

        //处理where条件
        handleWhereCriterial(userId, plain, mainTableName, permitTable);

        //处理排序条件
        handleOrderCriterial(plain, mainTableName);

        //增加join语句
        addJoinMethod(accessField, plain, mainTableName, permitTable);

        String plainStr = plain.toString();

        return plainStr;
    }

    /**
     * 处理where条件
     * @param userId
     * @param plain
     * @param mainTableName
     * @param permitTable
     * @throws JSQLParserException
     */
    private void handleWhereCriterial(Long userId, PlainSelect plain, String mainTableName, String permitTable) throws JSQLParserException {
        Expression where = plain.getWhere();

        String dataAuthSql = permitTable + ".receiver_id = " + userId;
        if (where == null) {
            plain.setWhere(CCJSqlParserUtil.parseCondExpression(dataAuthSql));
        } else {
            addTableAliasToColumns(mainTableName, plain);

            plain.setWhere(new AndExpression(where, CCJSqlParserUtil.parseCondExpression(dataAuthSql)));
        }
    }

    /**
     * 处理排序条件
     * @param plain
     * @param mainTableName
     * @throws JSQLParserException
     */
    private void handleOrderCriterial(PlainSelect plain, String mainTableName) throws JSQLParserException {
        List<OrderByElement> orderByElements = plain.getOrderByElements();
        if (CollectionUtils.isNotEmpty(orderByElements)) {
            for (OrderByElement orderByElement : orderByElements) {
                Expression expression = orderByElement.getExpression();
                String orderCol = expression.toString();
                orderCol = mainTableName + "." + orderCol;
                orderByElement.setExpression(CCJSqlParserUtil.parseCondExpression(orderCol));
            }
        }
    }

    /**
     * 增加join语句
     * @param accessField
     * @param plain
     * @param mainTableName
     * @param permitTable
     */
    private void addJoinMethod(String accessField, PlainSelect plain, String mainTableName, String permitTable) {
        Join join = new Join();
        join.setRight(true);
        join.setRightItem(new Table(permitTable));
        EqualsTo appendExpression = new EqualsTo();
        appendExpression.setLeftExpression(new StringValue(mainTableName + "." + accessField));
        appendExpression.setRightExpression(new StringValue(permitTable + ".content_id"));

        join.setOnExpression(appendExpression);

        plain.setJoins(Lists.newArrayList(join));
    }

    /**
     * 修改字段名称
     *
     * @param tableName
     * @param plainSelect
     */
    private void addTableAliasToColumns(String tableName, PlainSelect plainSelect) {
        plainSelect.getWhere().accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                column.setColumnName(tableName + "." + column.getColumnName());
            }
        });
    }

    /**
     * 获取方法上的数据权限注解
     *
     * @param mappedStatement
     * @return
     * @throws ClassNotFoundException
     */
    private RequireDataPermissions getAnnontation(MappedStatement mappedStatement) throws ClassNotFoundException {
        RequireDataPermissions requireDataPermissions = null;
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        final Method[] method = Class.forName(className).getMethods();
        for (Method me : method) {
            if (me.getName().equals(methodName) && me.isAnnotationPresent(RequireDataPermissions.class)) {
                requireDataPermissions = me.getAnnotation(RequireDataPermissions.class);
                break;
            }
        }
        return requireDataPermissions;
    }
}
