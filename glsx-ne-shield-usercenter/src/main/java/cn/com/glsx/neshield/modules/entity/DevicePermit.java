package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Accessors(chain = true)
@Data
@Table(name = "t_device_permit")
public class DevicePermit extends BaseEntity {

    /**
     * 授权人id
     */
    @Column(name = "accorder_id")
    private Long accorderId;

    /**
     * 获权人id
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 内容类型
     */
    @Column(name = "content_type")
    private Byte contentType;

    /**
     * 权限类型 0=读写 1=只读
     */
    @Column(name = "permit_type")
    private Byte permitType;

}