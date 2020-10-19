package cn.com.glsx.auth.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Role {

    private Long roleId;

    private String roleName;

    /**
     * 角色权限类型 0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部
     */
    private Integer rolePermissionType;

    private Long tenantId;

    private List<Permission> permissionList;

    private Integer roleVisibility;

    private String roleTenants;

}
