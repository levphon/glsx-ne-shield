package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.model.ParameterAnnotationHolder;
import cn.com.glsx.auth.model.RequireDataPermissions;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.common.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

/**
 * 处理针对某一或某些特定数据操作（增、删、改、详情）作数据权限校验
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
public class DataOperationPermissionInterceptor implements Interceptor {

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        //log.info("*************************咱的" + target.getClass().getName());

        if (target instanceof RoutingStatementHandler) {
            try {
                RoutingStatementHandler statementHandler = (RoutingStatementHandler) target;
                // MetaObject是mybatis里面提供的一个工具类，类似反射的效果
                MetaObject metaStatementHandler = MetaObject.forObject(statementHandler,
                        DEFAULT_OBJECT_FACTORY,
                        DEFAULT_OBJECT_WRAPPER_FACTORY,
                        REFLECTOR_FACTORY);

                MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");

                SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
                if (sqlCommandType != SqlCommandType.SELECT) {
                    return invocation.proceed();
                }

                RequireDataPermissions requireDataPermissions = getAnnontation(mappedStatement);

                Method method;
                String id = mappedStatement.getId();
                String className = id.substring(0, id.lastIndexOf("."));
                String methodName = id.substring(id.lastIndexOf(".") + 1);
                final Method[] methods = Class.forName(className).getMethods();
                for (Method me : methods) {
                    if (me.getName().equals(methodName) && me.isAnnotationPresent(RequireDataPermissions.class)) {
                        requireDataPermissions = me.getAnnotation(RequireDataPermissions.class);
                        method = me;
                        break;
                    }
                }

                if (requireDataPermissions == null || StringUtils.isEmpty(requireDataPermissions.linkField())) {
                    return invocation.proceed();
                }

                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
                String originSql = boundSql.getSql();
                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) boundSql.getParameterObject();

                ParameterAnnotationHolder holder = new ParameterAnnotationHolder();
                Integer type = holder.getType();
                String parameterName = holder.getParameterName();
                Object o = paramMap.get(parameterName);

                if (type == 1) {
                    ReflectUtils.getObjectValue(o);//获取值&类型 id
                    //creatorId
                    Long creatorId = 0L;

                    List<Long> creatorIds = ShieldContextHolder.getCreatorIds();
                    //是否有操作权限

                } else if (type == 2) {

                }

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