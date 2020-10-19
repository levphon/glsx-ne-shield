package cn.com.glsx.admin.services.notifyservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplateMessageDTO implements Serializable {

    private String from;
    private String to;
    private String title;
    private String content;
    private List<Map<String, String>> attachments;

}
