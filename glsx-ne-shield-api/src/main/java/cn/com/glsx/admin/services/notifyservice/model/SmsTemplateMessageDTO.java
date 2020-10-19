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
public class SmsTemplateMessageDTO implements Serializable {

    private String phone;
    private String template;
    private String content;
    private Map<String, Object> args;

}
