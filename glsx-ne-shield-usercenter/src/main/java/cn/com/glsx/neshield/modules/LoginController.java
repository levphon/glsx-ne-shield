package cn.com.glsx.neshield.modules;

import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.UserDTO;
import cn.com.glsx.neshield.modules.service.UserService;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author payu
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     */
    @SysLog(module = "系统登录", action = OperateType.LOGIN, saveRequest = false)
    @PostMapping(value = "/login")
    public R login(@RequestParam String account, @RequestParam String password) {

        User user = userService.findByAccount(account);

        boolean verifyFlag = verify(user, password);

        Map<String, Object> rtnMap = new HashMap<>();
        String token = userService.createToken(user);
        rtnMap.put("token", token);

        UserDTO userInfo = userService.userInfo(user.getId());
        rtnMap.put("user", userInfo);
        return R.ok().data(rtnMap);
    }

    private boolean verify(User user, String inputPassword) {
        //是否存在
        if (user == null) {
            throw new UserCenterException("账号不存在");
        }

        //是否停用
        if (!SysConstants.EnableStatus.enable.getCode().equals(user.getEnableStatus())) {
            throw new UserCenterException("账号已停用");
        }

        //密码是否ok
        boolean pwdFlag = userService.verifyPassword(user, inputPassword);
        if (!pwdFlag) {
            throw new UserCenterException("账号或密码不正确");
        }
        return true;
    }

    /**
     * 登出
     */
    @SysLog(module = "系统登出", action = OperateType.LOGOUT)
    @GetMapping(value = "/logout")
    public R logout() {
        // TODO: 2020/10/9
        return R.ok();
    }

}
