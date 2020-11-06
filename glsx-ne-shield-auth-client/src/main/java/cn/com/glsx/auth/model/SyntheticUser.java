package cn.com.glsx.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class SyntheticUser {

    private Long userId;

    /**
     * 用户账号
     */
    private String account;

    private boolean isAdmin;

//    private UserGroup userGroup;

    /**
     * 用户所属租户
     */
    private Tenant tenant;

    /**
     * 用户所属部门
     */
    private Department department;

    /**
     * 数据拥有者id
     */
    private List<Long> ownerIdList;

    /**
     * 用户角色
     */
    private List<Role> roleList;

    /**
     * 用户菜单权限
     */
    private List<MenuPermission> menuPermissionList;

}
