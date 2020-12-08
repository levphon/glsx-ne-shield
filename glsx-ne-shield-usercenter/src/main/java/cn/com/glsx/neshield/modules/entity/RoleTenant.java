package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Accessors(chain = true)
@Table(name = "t_role_tenant")
public class RoleTenant extends BaseEntity {

    /**
     * 角色id
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;

}