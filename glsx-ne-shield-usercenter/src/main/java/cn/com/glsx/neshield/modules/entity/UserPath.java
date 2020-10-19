package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Accessors(chain = true)
@Data
@Table(name = "t_user_path")
public class UserPath extends BaseEntity {

    /**
     * 上级用户
     */
    @Column(name = "superior_id")
    private Long superiorId;

    /**
     * 下级用户id
     */
    @Column(name = "sub_id")
    private Long subId;

    /**
     * 深度(下级节点-上级节点层数)
     */
    private Integer depth;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;

}