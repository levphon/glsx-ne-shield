package cn.com.glsx.admin.services.notifyservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatTemplateMessageDTO implements Serializable {

    private String appid;
    private String openid;
    private String templateId;
    private String url;
    private Map<String, String> data;

}
