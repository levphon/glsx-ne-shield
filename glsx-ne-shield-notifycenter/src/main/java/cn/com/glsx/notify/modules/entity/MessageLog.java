package cn.com.glsx.notify.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_message_log")
public class MessageLog extends BaseEntity {

    @Column(name = "from_user")
    private String fromUser;

    @Column(name = "to_user")
    private String toUser;

    private String template;

    private String subject;

    private String content;

    private String attachment;

    private String type;

}