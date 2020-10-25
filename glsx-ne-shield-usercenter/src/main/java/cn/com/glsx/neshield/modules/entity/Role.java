package cn.com.glsx.neshield.modules.entity;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Accessors(chain = true)
@Data
@Table(name = "t_role")
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @Column(name = "role_name")
    private String roleName;

    /**
     * 角色权限类型 0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部
     */
    @Column(name = "role_permission_type")
    private Integer rolePermissionType;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 最大拥有用户数
     */
    @Column(name = "max_user")
    private Long maxUser;

    /**
     * 互斥角色集合
     */
    @Column(name = "repel_roles")
    private String repelRoles;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 角色可见度（0=共享，1=系统管理员，2=指定租户）
     */
    @Column(name = "role_visibility")
    private Integer roleVisibility;

    /**
     * 角色租户范围，逗号分割
     */
    @Column(name = "role_tenants")
    private String roleTenants;

    @Column(name = "enable_status")
    private Integer enableStatus;

    public void setContextInfo(boolean isAdd){
        Date date = new Date();
        if (isAdd){
            setCreatedBy(ShieldContextHolder.getUserId());
            setCreatedDate(date);
        }
        setUpdatedBy(ShieldContextHolder.getUserId());
        setUpdatedDate(date);
    }

}