package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.*;
import cn.com.glsx.neshield.modules.model.OrgSuperiorModel;
import cn.com.glsx.neshield.modules.model.param.DepartmentSearch;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentCount;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;

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
    private UserPathMapper userPathMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 获取当前用户所拥有的部门权限
     *
     * @return
     */
    public List<Department> getCurrentUserDepartments() {
        Long roleId = ShieldContextHolder.getRoleId();
        Long userId = ShieldContextHolder.getUserId();
        List<Long> departmentIdList = getRoleDepartment(userId, roleId);

        return Lists.newArrayList();
    }

    /**
     * 获取当前用户权限所拥有的部门
     *
     * @return
     */
    public List<Long> getCurrentUserDepartmentIds() {
        Long roleId = ShieldContextHolder.getRoleId();
        Long userId = ShieldContextHolder.getUserId();
        return getRoleDepartment(userId, roleId);
    }

    public List<Long> getRoleDepartment(Long userId, Long roleId) {
        List<Long> departmentIdList = Lists.newArrayList();

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return departmentIdList;
        }

        if (oneself.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectById(userId);

            departmentIdList.add(user.getDepartmentId());
        } else if (all.getCode().equals(role.getRolePermissionType())) {
            List<Department> deptList = departmentMapper.selectAllNotDel();

            departmentIdList = deptList.stream().map(Department::getId).collect(Collectors.toList());
        } else if (selfDepartment.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectById(userId);

            departmentIdList.add(user.getDepartmentId());
        } else if (subDepartment.getCode().equals(role.getRolePermissionType())) {
            User user = userMapper.selectById(userId);

            List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(user.getDepartmentId());

            departmentIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());
        } else if (subordinate.getCode().equals(role.getRolePermissionType())) {
            List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);

            departmentIdList = departmentUserCountList.stream().map(DepartmentUserCount::getDepartmentId).collect(Collectors.toList());
        }
        return departmentIdList;
    }

    /**
     * 根部门列表
     *
     * @param search
     * @return
     */
    public PageInfo<DepartmentDTO> rootDepartmentList(OrganizationSearch search) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        Page page = null;

        DepartmentSearch deptSearch = new DepartmentSearch()
                .setEnableStatus(search.getEnableStatus())
                .setDepartmentName(search.getOrgName())
                .setIsRoot(Constants.IS_ROOT_DEPARTMENT);

        //非admin和授权全部数据的用户只能看到自己所在的组织
        if (!ShieldContextHolder.isRoleAdmin() && !all.getCode().equals(rolePermissionType)) {
            deptSearch.setTenantId(ShieldContextHolder.getTenantId());
        }

        // 2020/12/4 模糊搜索
        Set<Long> superiorIds = Sets.newHashSet();
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            //得到模糊查询得到的部门的所有上级id
            List<OrgSuperiorModel> orgSuperiorModelList = organizationMapper.selectSuperiorIdsByOrg(new OrgTreeSearch()
                    .setTenantId(deptSearch.getTenantId())
                    .setOrgName(deptSearch.getDepartmentName())
                    .setEnableStatus(deptSearch.getEnableStatus()));
            superiorIds = getSuperiorIds(orgSuperiorModelList);

            //置空模糊查询条件，根据上级id查根组织
            deptSearch.setOrgIds(superiorIds).setDepartmentName(null);
        }

        if (search.isForPage()) {
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        }

        List<Department> rootList = null;
        if (StringUtils.isNotEmpty(search.getOrgName()) && CollectionUtils.isEmpty(superiorIds)) {
            //如果模糊查询结果为空，根组织也不返回
            rootList = Lists.newArrayList();
        } else {
            rootList = departmentMapper.search(deptSearch);
        }

        List<DepartmentDTO> departmentDTOList = getDepartmentAssembled(rootList, search.isHasChild(), search.isHasUserNumber());

        PageInfo<DepartmentDTO> pageInfo = new PageInfo<>(departmentDTOList);
        if (search.isForPage()) {
            pageInfo.setPages(page.getPages());//总页数
            pageInfo.setTotal(page.getTotal());//总条数
        }
        return pageInfo;
    }

    public List<DepartmentDTO> childrenList(OrganizationSearch search) {

        Department department = departmentMapper.selectById(search.getRootId());

        // 2020/12/4 模糊搜索
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            //得到模糊查询得到的部门的所有上级id
            List<OrgSuperiorModel> orgSuperiorModelList = organizationMapper.selectSuperiorIdsByOrg(new OrgTreeSearch()
                    .setTenantId(department.getTenantId())
                    .setOrgName(search.getOrgName())
                    .setEnableStatus(search.getEnableStatus()));
            Set<Long> superiorIds = getSuperiorIds(orgSuperiorModelList);

            //置空模糊查询条件，根据上级id查根组织
            search.setTenantId(department.getTenantId()).setOrgIds(superiorIds).setOrgName(null);
        }

        List<Department> departmentList = organizationMapper.selectChildrenList(search);

        List<DepartmentDTO> departmentDTOList = this.getDepartmentAssembled(departmentList, true, false);

        if (ShieldContextHolder.isRoleAdmin() || all.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            return departmentDTOList;
        }

        //下面代码控制角色的可见权限
        List<Long> departmentIdList = this.getCurrentUserDepartmentIds();

        departmentDTOList = departmentDTOList.stream().filter(d -> departmentIdList.contains(d.getId())).collect(Collectors.toList());

        return departmentDTOList;
    }

    /**
     * 获取上级组织id
     *
     * @param superiorModelList
     * @return
     */
    public Set<Long> getSuperiorIds(List<OrgSuperiorModel> superiorModelList) {
        Set<Long> superiorIds = Sets.newHashSet();
        superiorModelList.forEach(osm -> {
            if (com.glsx.plat.common.utils.StringUtils.isNotEmpty(osm.getSuperiorIds())) {
                String[] ids = osm.getSuperiorIds().split(",");
                for (String id : ids) {
                    superiorIds.add(Long.valueOf(id));
                }
            }
        });
        return superiorIds;
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

        if (CollectionUtils.isNotEmpty(departmentIds)) {
            if (hasChild) {
                List<Organization> organizationList = organizationMapper.selectSubOrgList(departmentIds, 1);

                Map<Long, List<Long>> subOrganizationMap = organizationList.stream().collect(Collectors.toMap(Organization::getSuperiorId, org -> Lists.newArrayList(org.getSubId()),
                        (List<Long> newValueList, List<Long> oldValueList) -> {
                            oldValueList.addAll(newValueList);
                            return oldValueList;
                        }));

                departmentDTOList.forEach(dep -> dep.setHasChildren(subOrganizationMap.get(dep.getId()) != null));
            }

            if (hasUserNumber) {
                Map<Long, Integer> recursiveDepartmentUser = countRecursiveDepartmentUser(departmentIds);

                departmentDTOList.forEach(dep -> {
                    Integer number = recursiveDepartmentUser.get(dep.getId());
                    dep.setUserNumber(number == null ? 0 : number);
                });
            }
        }
        return departmentDTOList;
    }

    /**
     * 统计部门及下级部门用户数 （批量）
     *
     * @param departmentIds
     * @return
     */
    public Map<Long, Integer> countRecursiveDepartmentUser(List<Long> departmentIds) {
        HashMap<Long, Integer> departmentUserMap = Maps.newHashMap();
        //只得到存在下级的组织
        List<Organization> allSubList = organizationMapper.selectSubOrgList(departmentIds, null);

        Map<Long, List<Long>> subDepartmentIdListMap = allSubList.stream().collect(Collectors.groupingBy(Organization::getSuperiorId))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Organization::getSubId)
                        .collect(Collectors.toList())));

        //加上没有下级的组织
        departmentIds.forEach(item -> {
            if (!subDepartmentIdListMap.containsKey(item)) {
                subDepartmentIdListMap.put(item, Lists.newArrayList());
            }
        });

        List<Long> subDepartmentIdList = allSubList.stream().map(Organization::getSubId).distinct().collect(Collectors.toList());

        List<DepartmentCount> departmentCountList = userMapper.countDepartmentsUser(subDepartmentIdList);

        Map<Long, Integer> departmentUserNumberMap = departmentCountList.stream().collect(Collectors.toMap(DepartmentCount::getDepartmentId, DepartmentCount::getUserNumber));

        for (Map.Entry<Long, List<Long>> entry : subDepartmentIdListMap.entrySet()) {
            Long parentId = entry.getKey();
            List<Long> subIdList = entry.getValue();

            if (!subIdList.contains(parentId)) {
                subIdList.add(parentId);
            }

            Integer departmentUserNumber = 0;
            for (Long subId : subIdList) {
                if (departmentUserNumberMap.get(subId) != null) {
                    departmentUserNumber += departmentUserNumberMap.get(subId);
                }
            }
            departmentUserMap.put(entry.getKey(), departmentUserNumber);
        }
        return departmentUserMap;
    }

}
