package cn.com.glsx.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class SyntheticUser {

    private Long userId;

    private String username;

    private boolean isAdmin;

    private UserGroup userGroup;

    private Tenant tenant;

    private Department department;

    private List<Role> roleList;

    private List<MenuPermission> menuPermissionList;

}
