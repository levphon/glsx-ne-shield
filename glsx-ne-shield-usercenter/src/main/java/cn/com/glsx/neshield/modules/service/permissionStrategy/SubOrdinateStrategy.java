package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.mapper.UserPathMapper;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Component
public class SubOrdinateStrategy implements PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private UserPathMapper userPathMapper;

    /**
     * 4 subordinate
     * * 4.1 root找自己根部门
     * * 4.2 非root
     * * 找到rootId的下级部门alist
     * * 连表查出所有下级用户（及本人）所在所有部门blist
     * * t_organization查询blist是当前alist下级的，存在关系，则为最终部门列表clist
     * * -设置userNum 据前面查出的map
     * * -设置hasChild clist有depth>=1且在blist集合中的下级部门，则为true
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> organizationSimpleList(Long rootId) {

        List<DepartmentDTO> departmentDTOList;

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();
        Long userId = ShieldContextHolder.getUserId();

        //alist
        List<Long> rawSubIdList = Lists.newArrayList();

        if (rootId == null) {
            //     * 4.1 root找自己根部门
            Organization organization = organizationMapper.selectRootPath(userDeptId);

            Department department = departmentMapper.selectByPrimaryKey(organization.getSuperiorId());

            rawSubIdList.add(department.getId());
        } else {
            //     * 4.2 非root
            //     * 找到rootId的下级部门alist
            List<Organization> rawSubOrganizationList = organizationMapper.selectSubList(Lists.newArrayList(rootId), 1);

            rawSubIdList = rawSubOrganizationList.stream().map(Organization::getSubId).collect(Collectors.toList());
        }

        //     * 连表查出所有下级用户（及本人）所在所有部门blist
        List<DepartmentUserCount> subordinateDepartmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);

        Map<Long, Long> subordinateDepartmentUserCountMap = subordinateDepartmentUserCountList.stream().collect(Collectors.toMap(DepartmentUserCount::getDepartmentId, DepartmentUserCount::getUserNumber));

        List<Long> subordinateDepartmentIdList = subordinateDepartmentUserCountList.stream().map(DepartmentUserCount::getDepartmentId).collect(Collectors.toList());

        OrganizationBO organizationBO = new OrganizationBO().setSubIdList(subordinateDepartmentIdList).setSuperiorIdList(rawSubIdList);

        //     * t_organization查询blist是当前alist下级的，存在关系，则为最终部门列表clist
        List<Organization> finalOrganizationList = organizationMapper.selectList(organizationBO);

        Map<Long, List<Organization>> superSubOrganizationMap = finalOrganizationList.stream().collect(Collectors.groupingBy(Organization::getSuperiorId));

        List<Long> finalSubIdList = Lists.newArrayList(finalOrganizationList.stream().map(Organization::getSubId).collect(Collectors.toSet()));

        List<Department> finalDepartmentList = departmentMapper.selectByIds(finalSubIdList);

        departmentDTOList = departmentService.getDepartmentAssembled(finalDepartmentList, false, false);

        //     * -设置hasChild clist有depth>=1且在blist集合中的下级部门，则为true
        List<Organization> hasChildOrganizationList = organizationMapper.selectList(new OrganizationBO().setSuperiorIdList(finalSubIdList)
                .setSubIdList(subordinateDepartmentIdList).setBiggerDepth(1));

        List<Long> superiorIdList = hasChildOrganizationList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        for (DepartmentDTO departmentDTO : departmentDTOList) {
            Long id = departmentDTO.getId();

            List<Organization> subOrganizationList = superSubOrganizationMap.get(id);

            //     * -设置userNum 据前面查出的map
            Long userNumber = 0L;
            for (Organization organization : subOrganizationList) {
                Long userCount = subordinateDepartmentUserCountMap.get(organization.getSubId());

                userNumber += userCount;
            }

            departmentDTO.setUserNumber(userNumber);
            departmentDTO.setHasChildren(superiorIdList.contains(id));
        }

        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(String departmentName) {

        Long userId = ShieldContextHolder.getUserId();

        List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);

        List<Long> subDepartmentIdList = departmentUserCountList.stream().map(DepartmentUserCount::getDepartmentId).collect(Collectors.toList());

        List<Department> namedDepartmentList = departmentMapper.selectDepartmentList(new Department().setDepartmentName(departmentName));

        List<Department> finalNamedDepartmentList = Lists.newArrayList();
        for (Department department : namedDepartmentList) {
            if (subDepartmentIdList.contains(department.getId())){
                finalNamedDepartmentList.add(department);
            }
        }

        //符合条件的部门idlist
        List<Long> nameDepartmentIdList = finalNamedDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        List<Organization> superiorOrgList = organizationMapper.selectList(new OrganizationBO().setSubIdList(nameDepartmentIdList));
        //所有部门id
        List<Long> allDepartmentIdList = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        List<Department> allDepartmentList = departmentMapper.selectByIds(allDepartmentIdList);
        //所有组织链
        List<Organization> organizationList = organizationMapper.selectList(new OrganizationBO().setSubIdList(allDepartmentIdList).setSuperiorIdList(allDepartmentIdList));

        //根节点
        List<Organization> rootList = organizationMapper.selectRootIdList(nameDepartmentIdList);
        Set<Long> rootIds = rootList.stream().map(Organization::getSuperiorId).collect(Collectors.toSet());

        Map<Long, Organization> organizationMap = organizationList.stream().filter(m -> m.getDepth() == 1).
                collect(Collectors.toMap(Organization::getSubId, treeModel -> treeModel));

        List<DepartmentUserCount> subordinateDepartmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);
        Map<Long, Long> subordinateDepartmentUserCountMap = subordinateDepartmentUserCountList.stream().collect(Collectors.toMap(DepartmentUserCount::getDepartmentId, DepartmentUserCount::getUserNumber));

        List<OrgModel> modelList = allDepartmentList.stream().map(dep -> {
            OrgModel orgModel = new OrgModel();
            Organization organization = organizationMap.get(dep.getId());
            if (organization != null) {
                orgModel.setParentId(organization.getSuperiorId());
            }
            orgModel.setId(dep.getId());
            orgModel.setDeptName(dep.getDepartmentName());
            orgModel.setTenantId(dep.getTenantId());
            Long userNumber = subordinateDepartmentUserCountMap.get(dep.getId());
            orgModel.setUserNumber(userNumber);
            return orgModel;
        }).collect(Collectors.toList());
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).sorted(Comparator.comparing(OrgTreeModel::getOrderNum)).collect(Collectors.toList());
        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertClosure(orgTreeModelList, Lists.newArrayList(rootIds));

        return orgTree;
    }
}
