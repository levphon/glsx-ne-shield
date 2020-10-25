package cn.com.glsx.neshield.modules;

import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.LoginBO;
import cn.com.glsx.neshield.modules.model.view.UserDTO;
import cn.com.glsx.neshield.modules.service.UserService;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.core.web.R;
import com.glsx.plat.web.utils.SessionUtils;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author payu
 */
@Slf4j
@RefreshScope
@RestController
public class LoginController {

    @Value("${captcha.text}")
    private String captchaText;

    @Resource
    private Producer producer;

    @Autowired
    private UserService userService;

    @GetMapping("captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //生成文字验证码
        String text = producer.createText();
        if (StringUtils.isNotEmpty(captchaText)) {
            text = captchaText;
        }

        //生成图片验证码
        BufferedImage image = producer.createImage(text);

        //保存到 session
        request.getSession().setAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY, text);

        ServletOutputStream out = response.getOutputStream();

        ImageIO.write(image, "jpg", out);
    }

    /**
     * 登录
     */
    @SysLog(module = "系统登录", action = OperateType.LOGIN, saveRequest = false)
    @PostMapping(value = "/login")
    public R login(@RequestBody @Valid LoginBO loginBO) {

        //verifyCaptcha(loginBO.getCaptcha());

        User user = userService.findByAccount(loginBO.getAccount());

        verify(user, loginBO.getPassword());

        Map<String, Object> rtnMap = new HashMap<>();
        String token = userService.createToken(user);
        rtnMap.put("token", token);

        UserDTO userInfo = userService.userInfo(user.getId());
        rtnMap.put("user", userInfo);
        return R.ok().data(rtnMap);
    }

    /**
     * 验证码校验
     *
     * @param captcha
     */
    protected void verifyCaptcha(String captcha) {
        HttpServletRequest request = SessionUtils.request();
        // 从session中获取图形吗字符串
        String kaptcha = (String) request.getSession().getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        //删除缓存验证码
        request.getSession().removeAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        // 校验
        if (kaptcha == null || !kaptcha.equals(captcha)) {
            throw new UserCenterException("验证码错误");
        }
    }

    private void verify(User user, String inputPassword) {
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
