package cn.com.glsx.auth.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SyntheticUser {

    private Long userId;

    /**
     * 用户账号
     */
    private String account;

    private boolean isAdmin;

    /**
     * 用户组
     */
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
     * 用户角色
     */
    private List<Role> roles;

    /**
     * 数据创建人部门id
     */
    private Set<Long> visibleDeptIds;

    /**
     * 数据创建人id
     */
    private Set<Long> visibleCreatorIds;

}
