package cn.com.glsx.notify.modules.api;

import cn.com.glsx.admin.api.NotifyCenterFeignClient;
import cn.com.glsx.admin.services.notifyservice.model.MailTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.PCTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.SmsTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.WechatTemplateMessageDTO;
import cn.com.glsx.notify.modules.service.MailService;
import cn.com.glsx.notify.modules.service.PCService;
import cn.com.glsx.notify.modules.service.SmsService;
import cn.com.glsx.notify.modules.service.WeixinService;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/notify")
class NotifyApiController implements NotifyCenterFeignClient {

    @Autowired
    private SmsService smsService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PCService pcService;

    @Autowired
    private WeixinService weixinService;

    @Override
    @PostMapping(value = "/sendSms")
    public R sendSms(@RequestBody SmsTemplateMessageDTO messageDTO) {
        smsService.sendSms(messageDTO.getPhone(), messageDTO.getTemplate(), messageDTO.getArgs().values());
        return R.ok();
    }

    @Override
    @PostMapping(value = "/sendMail")
    public R sendMail(@RequestBody MailTemplateMessageDTO messageDTO) {
        Map<String, Object> args = new HashMap<>();
        args.put("arg1", "刘宇峰");
        args.put("arg2", "B73493098765");
        args.put("arg3", 1234);
        mailService.sendEmail("liuyf@didihu.com.cn", "notifycenter test", "MAIL_TEMPLATE_ORDER_PAID_NOTIFY_OPERATOR", args.values());
        return R.ok();
    }

    @Override
    @PostMapping(value = "/sendPC")
    public R sendPC(@RequestBody PCTemplateMessageDTO messageDTO) {
        // TODO: 2020/9/21 名称约束

        // 2020/9/21 kafka消息队列
        pcService.sendMessage(messageDTO);
        return R.ok();
    }

    @Override
    @PostMapping(value = "/sendWechat")
    public R sendWechat(@RequestBody WechatTemplateMessageDTO messageDTO) {
        weixinService.push(messageDTO.getAppid(), messageDTO);
        return R.ok();
    }

}
