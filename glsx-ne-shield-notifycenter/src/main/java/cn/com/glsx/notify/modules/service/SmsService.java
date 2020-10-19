package cn.com.glsx.notify.modules.service;

import cn.com.glsx.notify.modules.entity.MessageLog;
import cn.com.glsx.notify.modules.entity.MessageTemplate;
import cn.com.glsx.notify.modules.mapper.MessageLogMapper;
import cn.com.glsx.notify.modules.mapper.MessageTemplateMapper;
import cn.com.glsx.notify.modules.utils.SmsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * @author payu
 */
@Service
public class SmsService {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private MessageLogMapper messageLogMapper;

    @Autowired
    private MessageTemplateMapper messageTemplateMapper;

    public void sendSms(String phone, String template, Object... arg) {
        MessageTemplate mt = messageTemplateMapper.findByTemplate(template);
        if (mt == null) return;

        String content = MessageFormat.format(mt.getContent(), arg);
        smsUtils.send(phone, content);

        MessageLog log = new MessageLog();
        log.setToUser(phone);
        log.setContent(content);
        log.setTemplate(template);
        log.setType(mt.getType());
        messageLogMapper.insert(log);
    }

}
