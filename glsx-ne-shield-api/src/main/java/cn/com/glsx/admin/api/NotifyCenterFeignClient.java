package cn.com.glsx.admin.api;

import cn.com.glsx.admin.services.notifyservice.model.MailTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.PCTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.SmsTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.WechatTemplateMessageDTO;
import com.glsx.plat.core.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "glsx-ne-shield-notifycenter", contextId = "notifycenter", path = "/api/notify/", decode404 = true)
public interface NotifyCenterFeignClient {

    @PostMapping(value = "/sendSms", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R sendSms(@RequestBody SmsTemplateMessageDTO messageDTO);

    @PostMapping(value = "/sendMail", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R sendMail(@RequestBody MailTemplateMessageDTO messageDTO);

    @PostMapping(value = "/sendPC", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R sendPC(@RequestBody PCTemplateMessageDTO messageDTO);

    @PostMapping(value = "/sendWechat", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    R sendWechat(@RequestBody WechatTemplateMessageDTO messageDTO);

}
