package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.api.AuthFeignClient;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 授权验证拦截器
 *
 * @author payu
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthFeignClient authFeignClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ShieldContextHolder.setUser(null);
        ShieldContextHolder.removeUser();

        SyntheticUser user = null;

        R r = authFeignClient.getAuthUser();
        if (r.isSuccess()) {
            Object obj = r.getData();
            if (obj instanceof JSONObject) {
                JSONObject userJO = (JSONObject) obj;
                user = JSON.toJavaObject(userJO, SyntheticUser.class);
            } else if (obj instanceof SyntheticUser) {
                user = (SyntheticUser) obj;
            }
        }

        if (user != null) {
            ShieldContextHolder.setUser(user);
            return true;
        }

        //需要登录
        needLogin(response);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ShieldContextHolder.setUser(null);
        ShieldContextHolder.removeUser();
    }

    private void needLogin(HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(R.error(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg())));
            writer.flush();
        }
    }

}
