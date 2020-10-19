package cn.com.glsx.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class SyntheticUser {

    private Long userId;

    private String username;

    private UserGroup userGroup;

    private Department department;

    private List<Role> roleList;

    private Long roleId;

    private List<Menu> menuList;

    private Long tenantId;

}
