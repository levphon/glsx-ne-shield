package cn.com.glsx.notify.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Table;

@Data
@Table(name = "t_message_template")
public class MessageTemplate extends BaseEntity {

    private String template;

    private String subject;

    private String content;

    private String type;

}