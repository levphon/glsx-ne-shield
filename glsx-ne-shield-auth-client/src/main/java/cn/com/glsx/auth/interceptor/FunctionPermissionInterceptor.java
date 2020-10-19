package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.model.*;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: taoyr
 **/
@Component
public class FunctionPermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            RequireFunctionPermissions permissionRequired = ((HandlerMethod) handler).getMethodAnnotation(RequireFunctionPermissions.class);
            if (permissionRequired == null) {
                return true;
            }

            SyntheticUser currentUser = ShieldContextHolder.getUser();
            if (currentUser == null) {
                return true;
            }

            String url = request.getRequestURI();

            List<Permission> userPermissionList = ShieldContextHolder.getUserPermissions();

            boolean isPermit = userPermissionList.stream().map(Permission::getInterfaceUrl).collect(Collectors.toList()).contains(url);

            return isPermit;

        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}