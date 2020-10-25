package cn.com.glsx.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class Role {

    private Long tenantId;

    private Long roleId;

    private String roleName;

    /**
     * 角色权限类型 0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部
     */
    private Integer rolePermissionType;

    private Integer roleVisibility;

    private String roleTenants;

    private List<MenuPermission> menuPermissionList;

}
