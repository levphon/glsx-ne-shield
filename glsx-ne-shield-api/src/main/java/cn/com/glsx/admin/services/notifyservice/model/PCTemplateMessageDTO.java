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
public class PCTemplateMessageDTO implements Serializable {

    private String application;//应用服务，也是xxx_notify_topic
    private String from;//消息来源
    private String to;//对应应用用户标识
    private String template;
    private String content;
    private Map<String, Object> args;
    private Integer type;//推送方式：1私信，2广播

}
