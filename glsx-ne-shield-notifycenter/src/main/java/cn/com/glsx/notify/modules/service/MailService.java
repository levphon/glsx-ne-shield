package cn.com.glsx.notify.modules.service;

import cn.com.glsx.notify.modules.entity.MessageLog;
import cn.com.glsx.notify.modules.entity.MessageTemplate;
import cn.com.glsx.notify.modules.mapper.MessageLogMapper;
import cn.com.glsx.notify.modules.mapper.MessageTemplateMapper;
import com.glsx.plat.mail.utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * @author payu
 */
@Service
public class MailService {

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private MessageLogMapper messageLogMapper;

    @Autowired
    private MessageTemplateMapper messageTemplateMapper;

    public void sendEmail(String to, String title, String template, Object... arg) {
        MessageTemplate mt = messageTemplateMapper.findByTemplate(template);
        if (mt == null) return;

        String content = MessageFormat.format(mt.getContent(), arg);
        mailUtils.sendTextMail(to, title, content);

        MessageLog log = new MessageLog();
        log.setSubject(title);
        log.setToUser(to);
        log.setContent(content);
        log.setTemplate(template);
        log.setType(mt.getType());
        messageLogMapper.insert(log);
    }

}
