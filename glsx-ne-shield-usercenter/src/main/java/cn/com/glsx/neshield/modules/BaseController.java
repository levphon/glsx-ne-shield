package cn.com.glsx.neshield.modules;

import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.service.UserService;
import com.glsx.plat.core.web.R;
import com.glsx.plat.web.controller.AbstractController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author liuyf
 * @Title BaseController.java
 * @Package com.glsx.vasp.controller
 * @Description
 * @date 2019年10月24日 下午2:24:00
 */
@RestController
public class BaseController extends AbstractController {

    @Resource
    private UserService userService;

    /**
     * 这个给Spring Boot Admin探测用
     *
     * @return
     */
    @GetMapping(value = "/")
    public R index() {
        return R.ok("You get it!");
    }

    /**
     * 从session中获取当前用户
     *
     * @return
     */
    @Override
    public User getSessionUser() {
        return userService.getByToken();
    }

    @Override
    public Long getUserId() {
        User user = getSessionUser();
        if (user != null) {
            return user.getId();
        }
        return null;
    }

    @Override
    public String getAccount() {
        User user = getSessionUser();
        if (user != null) {
            return user.getAccount();
        }
        return null;
    }

}
