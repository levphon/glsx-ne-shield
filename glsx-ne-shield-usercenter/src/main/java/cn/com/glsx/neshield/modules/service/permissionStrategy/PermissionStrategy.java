package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import com.glsx.plat.common.model.TreeModel;

import java.util.List;

/**
 * @author taoyr
 */
public interface PermissionStrategy {

    /**
     * 根据角色数据权限范围获取对应的部门（组织）
     *
     * @return
     */
    List<Department> permissionDepartments();

    /**
     * 根据角色数据权限范围获取对应的人
     *
     * @return
     */
    List<User> permissionUsers();

    /**
     * 获取组织机构列表，需求特殊处理（比如本人角色数据权限为本人，但是要求能看的他自己的（包含自己建和非自己建）上级组织结构数据）
     *
     * @param rootId
     * @return
     */
    List<DepartmentDTO> organizationSimpleList(Long rootId);

    /**
     * 获取组织树
     *
     * @param departmentName
     * @return
     */
    List<? extends TreeModel> orgTree(String departmentName);

}
