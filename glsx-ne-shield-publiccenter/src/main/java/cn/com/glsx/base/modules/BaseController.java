package cn.com.glsx.base.modules;

import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.core.web.R;
import com.glsx.plat.web.controller.AbstractController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyf
 * @Title BaseController.java
 * @Package com.glsx.vasp.controller
 * @Description
 * @date 2019年10月24日 下午2:24:00
 */
@RestController
public class BaseController extends AbstractController {

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
    public SyntheticUser getSessionUser() {
        return ShieldContextHolder.getUser();
    }

    @Override
    public Long getUserId() {
        return ShieldContextHolder.getUserId();
    }

    @Override
    public String getAccount() {
        return ShieldContextHolder.getAccount();
    }

}
