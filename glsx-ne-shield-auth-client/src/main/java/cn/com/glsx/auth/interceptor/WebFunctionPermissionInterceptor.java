package cn.com.glsx.auth.interceptor;

import cn.com.glsx.auth.model.MenuPermission;
import cn.com.glsx.auth.model.RequireFunctionPermissions;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.alibaba.fastjson.JSON;
import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能菜单权限
 *
 * @author: taoyr
 **/
@Slf4j
@Component
public class WebFunctionPermissionInterceptor implements HandlerInterceptor {

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

            String uri = request.getRequestURI();
            log.info("RequestURI:" + uri);

            List<MenuPermission> menuPermissionList = ShieldContextHolder.getUserMenuPermissions();

            boolean isPermit = menuPermissionList.stream().map(MenuPermission::getInterfaceUrl).collect(Collectors.toList()).contains(uri);
            if (!isPermit) {
                needPermission(response);
                return false;
            }
        }
        return true;
    }

    private void needPermission(HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(R.error(
                    SystemMessage.OPERATE_PERMISSION_DENIED.getCode(),
                    SystemMessage.OPERATE_PERMISSION_DENIED.getMsg())
            ));
            writer.flush();
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
