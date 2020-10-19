package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.*;
import com.github.pagehelper.PageHelper;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentCount;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;
import static com.glsx.plat.core.enums.SysConstants.DeleteStatus.delete;

/**
 * @author: taoyr
 **/
@Service
public class DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserService userService;

    private static final int IS_ROOT_DEPARTMENT = 1;

    private static final int IS_NOT_ROOT_DEPARTMENT = 0;

    @Resource
    private UserPathMapper userPathMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 获取当前用户所拥有的部门权限
     *
     * @return
     */
    public List<Department> getCurrentUserDepartment() {
        Long roleId = ShieldContextHolder.getRoleId();
        Long userId = ShieldContextHolder.getUserId();

        return getRoleDepartment(userId, roleId);
    }

    /**
     * 获取用户所拥有的部门权限
     *
     * @param userId
     * @return
     */
    public List<Department> getUserDepartment(Long userId) {
        List<Role> roles = roleMapper.selectUserRoleList(userId);
        if (CollectionUtils.isEmpty(roles) || roles.get(0) == null) {
            return Lists.newArrayList();
        }

        return getRoleDepartment(userId, roles.get(0).getId());
    }

    public List<Department> getRoleDepartment(Long userId, Long roleId) {
        List<Department> departmentList = Lists.newArrayList();

        Role role = roleMapper.selectByPrimaryKey(roleId);

        if (role == null || delete.getCode().equals(role.getDelFlag())) {
            return departmentList;
        }

        if (oneself.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectByPrimaryKey(userId);
            Long departmentId = user.getDepartmentId();

            Department department = departmentMapper.selectByPrimaryKey(departmentId);
            departmentList.add(department);

            return departmentList;
        } else if (all.getCode().equals(role.getRolePermissionType())) {
            return departmentMapper.selectAllNotDel();
        } else if (selfDepartment.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectByPrimaryKey(userId);
            Long departmentId = user.getDepartmentId();

            Department department = departmentMapper.selectByPrimaryKey(departmentId);
            departmentList.add(department);

            return departmentList;
        } else if (subDepartment.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectByPrimaryKey(userId);
            Long departmentId = user.getDepartmentId();

            List<Organization> organizationList = organizationMapper.selectByRootId(departmentId);
            List<Long> departmentIds = organizationList.stream().map(Organization::getSubId).collect(Collectors.toList());

            return departmentMapper.selectByIds(departmentIds);
        } else if (subordinate.getCode().equals(role.getRolePermissionType())) {

            List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);
            List<Long> departmentIdList = departmentUserCountList.stream().map(DepartmentUserCount::getDepartmentId).collect(Collectors.toList());

            return departmentMapper.selectByIds(departmentIdList);
        } else {
            return departmentList;
        }
    }

    public Department getDepartmentById(Long departmentId) {
        return departmentMapper.selectByPrimaryKey(departmentId);
    }

    /**
     * 跟部门列表
     *
     * @param organizationSearch
     * @return
     */
    public R rootDepartmentList(OrganizationSearch organizationSearch) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        List<DepartmentDTO> departmentDTOList;

        List<Department> rootList = Lists.newArrayList();

        if (!ShieldContextHolder.isRoleAdmin() && !all.getCode().equals(rolePermissionType)) {
            //只能看自己公司的
            Long rootDepartmentId = ShieldContextHolder.getRootDepartmentId();
            Department department = departmentMapper.selectByPrimaryKey(rootDepartmentId);

            rootList.add(department);
        } else {
            Department department = new Department().setIsRoot(IS_ROOT_DEPARTMENT).
                    setEnableStatus(organizationSearch.getEnableStatus()).setDepartmentName(organizationSearch.getOrganizationName());
            department.setDelFlag(0);

            if (organizationSearch.isForPage()) {
                PageHelper.startPage(organizationSearch.getPageNumber(), organizationSearch.getPageSize());
            }
            rootList = departmentMapper.selectDepartmentList(department);
        }

        departmentDTOList = getDepartmentAssembled(rootList, true, false);

        return R.ok().data(departmentDTOList);
    }

    /**
     * 封装部门数据
     *
     * @param departmentList
     * @param hasChild
     * @param hasUserNumber
     * @return
     */
    public List<DepartmentDTO> getDepartmentAssembled(List<Department> departmentList, final boolean hasChild, final boolean hasUserNumber) {

        List<Long> departmentIds = departmentList.stream().map(Department::getId).collect(Collectors.toList());

        List<DepartmentDTO> departmentDTOList = departmentList.stream().map(dep -> {
                    DepartmentDTO departmentDTO = new DepartmentDTO();
                    BeanUtils.copyProperties(dep, departmentDTO);
                    return departmentDTO;
                }
        ).collect(Collectors.toList());

        if (hasChild) {
            List<Organization> organizationList = organizationMapper.selectSubList(departmentIds, 1);

            Map<Long, Long> subOrganizationMap = organizationList.stream().collect(Collectors.toMap(Organization::getSuperiorId, Organization::getSubId));

            departmentDTOList.forEach(dep -> dep.setHasChildren(subOrganizationMap.get(dep.getId()) != null));
        }

        if (hasUserNumber) {
            Map<Long, Long> recursiveDepartmentUser = countRecursiveDepartmentUser(departmentIds);

            departmentDTOList.forEach(dep -> dep.setUserNumber(recursiveDepartmentUser.get(dep.getId())));
        }

        return departmentDTOList;
    }

    /**
     * 统计部门及下级部门用户数 （批量）
     *
     * @param departmentIds
     * @return
     */
    public Map<Long, Long> countRecursiveDepartmentUser(List<Long> departmentIds) {
        HashMap<Long, Long> departmentUserMap = Maps.newHashMap();

        List<Organization> allSubList = organizationMapper.selectSubList(departmentIds, null);

        Map<Long, List<Long>> departmentSubIdListMap = allSubList.stream().collect(Collectors.groupingBy(Organization::getSuperiorId))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Organization::getSubId).collect(Collectors.toList())));

        List<Long> subDepartmentIdList = Lists.newArrayList(allSubList.stream().map(Organization::getSubId).collect(Collectors.toSet()));

        List<DepartmentCount> departmentCountList = userService.countDepartmentUserNumber(subDepartmentIdList);
        Map<Long, Long> departmentUserNumberMap = departmentCountList.stream().collect(Collectors.toMap(DepartmentCount::getDepartmentId, DepartmentCount::getUserNumber));

        for (Map.Entry<Long, List<Long>> entry : departmentSubIdListMap.entrySet()) {
            List<Long> subIdList = entry.getValue();

            Long departmentUserNumber = 0L;
            for (Long subId : subIdList) {
                Long subUserNumber = departmentUserNumberMap.get(subId);
                if (subUserNumber != null) {
                    departmentUserNumber += subUserNumber;
                }
            }

            departmentUserMap.put(entry.getKey(), departmentUserNumber);
        }

        return departmentUserMap;
    }


    /**
     * 统计部门及下级部门用户数
     *
     * @param departmentId
     * @return
     */
    public Long countRecursiveDepartmentUser(Long departmentId) {
        List<Organization> subList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);

        List<Long> subDepartmentIdList = Lists.newArrayList(subList.stream().map(Organization::getSubId).collect(Collectors.toSet()));

        List<DepartmentCount> departmentCountList = userService.countDepartmentUserNumber(subDepartmentIdList);
        Map<Long, Long> departmentUserNumberMap = departmentCountList.stream().collect(Collectors.toMap(DepartmentCount::getDepartmentId, DepartmentCount::getUserNumber));

        Long departmentUserNumber = 0L;

        for (Map.Entry<Long, Long> entry : departmentUserNumberMap.entrySet()) {
            Long userNumber = entry.getValue();

            departmentUserNumber += userNumber;
        }

        return departmentUserNumber;
    }
}
