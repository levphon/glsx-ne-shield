package cn.com.glsx.neshield.modules.api;

import cn.com.glsx.auth.api.AuthFeignClient;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.neshield.modules.service.UserService;
import com.glsx.plat.core.web.R;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.web.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthApiController implements AuthFeignClient {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils<ComJwtUser> jwtUtils;

    @Override
    @GetMapping("/isLogin")
    public R isLogin() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        boolean verifyFlag = jwtUtils.verifyToken(token);
        return R.ok().data(verifyFlag);
    }

    @Override
    @GetMapping("/getAuthUser")
    public R getAuthUser() {
        SyntheticUser authUser = userService.getSyntheticUser();
        return R.ok().data(authUser);
    }

    @Override
    @GetMapping("/getRelationAuthUserIds")
    public R getRelationAuthUserIds() {
        List<Long> userIdList = userService.getRelationAuthUserIds();
        return R.ok().data(userIdList);
    }

}
