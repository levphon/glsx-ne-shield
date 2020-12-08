package cn.com.glsx.auth.api;

import com.glsx.plat.core.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author payu
 */
@FeignClient(name = "glsx-ne-shield-usercenter", contextId = "authcenter", path = "/usercenter/auth/")
public interface AuthFeignClient {

    /**
     * 判断用户是否登录
     *
     * @return
     */
    @GetMapping(value = "/isLogin", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R isLogin();

    /**
     * 获取当前登录用户对应资源等权限
     *
     * @return
     */
    @GetMapping(value = "/getAuthUser", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R getAuthUser();

    /**
     * 获取当前登录用户角色数据权限内的部门id
     *
     * @return
     */
    @GetMapping(value = "/getAuthDeptIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R getAuthDeptIds();

    /**
     * 获取当前登录用户角色数据权限内的用户id
     *
     * @return
     */
    @GetMapping(value = "/getAuthUserIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R getAuthUserIds();

    /**
     * 获取当前登录用户角色授权的功能菜单
     *
     * @return
     */
    @GetMapping(value = "/getPermMenus", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R getPermMenus();

}
