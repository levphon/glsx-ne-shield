package cn.com.glsx.neshield.modules.api;

import cn.com.glsx.admin.api.UserCenterFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微服务应用在网关中处理统一返回体
 *
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/user")
public class UserApiController implements UserCenterFeignClient {



}
