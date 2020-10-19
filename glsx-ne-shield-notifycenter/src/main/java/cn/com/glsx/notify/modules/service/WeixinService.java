package cn.com.glsx.notify.modules.service;

import cn.com.glsx.admin.services.notifyservice.model.WechatTemplateMessageDTO;
import com.glsx.plat.wechat.modules.service.WxPushService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author payu
 */
@Service
public class WeixinService {

    @Autowired
    private WxPushService wxPushService;

    public void push(String appid, WechatTemplateMessageDTO wtmm) {
        WxMpTemplateMessage wechatTemplate = new WxMpTemplateMessage();
        wechatTemplate.setTemplateId(wtmm.getTemplateId());
        wechatTemplate.setToUser(wtmm.getOpenid());
        wechatTemplate.setUrl(wtmm.getUrl());

        List<WxMpTemplateData> data = new ArrayList<>();
        for (Map.Entry<String, String> entry : wtmm.getData().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.toLowerCase().contains("keyword")) {
                data.add(new WxMpTemplateData(key, value, "#173177"));
            } else {
                data.add(new WxMpTemplateData(key, value));
            }
        }
        wechatTemplate.setData(data);
        wxPushService.push(appid, wechatTemplate);
    }

}
