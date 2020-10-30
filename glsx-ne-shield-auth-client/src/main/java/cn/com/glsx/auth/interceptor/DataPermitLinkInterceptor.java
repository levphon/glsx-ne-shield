package cn.com.glsx.auth.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;

/**
 * 二期后面绝对权限,分享的业务数据权限控制（读、写）
 *
 * @author payu
 */
@Slf4j
//@Component
//@Intercepts({
//        @Signature(
//                type = StatementHandler.class,
//                method = "prepare",
//                args = {Connection.class, Integer.class}
//        )
//})
public class DataPermitLinkInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

}
