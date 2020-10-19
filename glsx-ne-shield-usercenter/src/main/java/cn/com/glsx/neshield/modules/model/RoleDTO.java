package cn.com.glsx.neshield.modules.model;

import cn.com.glsx.neshield.modules.entity.Department;
import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {

    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限类型 0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部
     */
    private Integer rolePermissionType;

    /**
     * 最大拥有用户数
     */
    private Long maxUser;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色可见度（0=共享，1=系统管理员，2=指定租户）
     */
    private Integer roleVisibility;

    /**
     * 角色租户范围，逗号分割
     */
    private List<Department> roleDepartments;

}
