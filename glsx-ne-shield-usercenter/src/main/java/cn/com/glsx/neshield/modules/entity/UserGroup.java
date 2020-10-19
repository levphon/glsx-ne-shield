package cn.com.glsx.neshield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;

@Accessors(chain = true)
@Data
@Table(name = "t_user_group")
public class UserGroup extends BaseEntity {

    /**
     * 用户组名称
     */
    @Column(name = "user_group_name")
    private String userGroupName;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 开启状态
     */
    @Column(name = "enableStatus")
    private Integer enableStatus;

}