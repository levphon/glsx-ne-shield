package cn.com.glsx.test;

import cn.com.glsx.Application;
import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.admin.services.notifyservice.model.PCTemplateMessageDTO;
import cn.com.glsx.admin.services.notifyservice.model.WechatTemplateMessageDTO;
import cn.com.glsx.notify.modules.service.MailService;
import cn.com.glsx.notify.modules.service.PCService;
import cn.com.glsx.notify.modules.service.SmsService;
import cn.com.glsx.notify.modules.service.WeixinService;
import com.glsx.plat.common.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotifyApplicationTests {

    @Autowired
    private SmsService smsService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PCService pcService;

    @Autowired
    private WeixinService weixinService;

    @Test
    public void testSms() {
        Map<String, Object> args = new HashMap<>();
        args.put("code", 1234);
        smsService.sendSms("18682185876", "SMS_TEMPLATE_VERIFY_CODE", args.values());
    }

    @Test
    public void testMail() {
        Map<String, Object> args = new HashMap<>();
        args.put("arg1", "刘宇峰");
        args.put("arg2", "B73493098765");
        args.put("arg3", 1234);
        mailService.sendEmail("liuyf@didihu.com.cn", "notifycenter test", "MAIL_TEMPLATE_ORDER_PAID_NOTIFY_OPERATOR", args.values());
    }

    @Test
    public void testPC() {
        PCTemplateMessageDTO templateMessageDTO = PCTemplateMessageDTO.builder()
                .application(Constants.SERVER_NAME + "_TOPIC")
                .from("alarmcenter")
                .to("1")
                .template("test")
                .content("设备离线报警")
                .type(1)
                .build();
        pcService.sendMessage(templateMessageDTO);
    }

    @Test
    public void testWeixinSend() {
        Map<String, String> data = new HashMap<>();
        data.put("first", "您的趣融贷款账单已出，" + DateUtils.formatNormal(new Date()) + "是您的趣融贷款还款日。");
        data.put("keyword1", "1856.65" + "元");
        data.put("keyword2", "第" + 5 + "期");
        data.put("remark", "如有疑问，请联系客服，客服热线：400-8060-999");

        WechatTemplateMessageDTO model = WechatTemplateMessageDTO.builder()
                .templateId("nclNdUjGPpXgQQth55Cx2BoJQscWlcKkEy97uZ9MXK0")
                .openid("oxAQwwMiYESDE8--YOopaAl6F4uo")
                .url("http://www.baidu.com")
                .data(data).build();
        weixinService.push("wxa9b785873a4b0793", model);
    }

}