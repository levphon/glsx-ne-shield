package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.admin.common.constant.UserConstants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class SubDepartmentStrategy implements PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Override
    public List<Department> permissionDepartments() {

        Long departmentId = ShieldContextHolder.getDepartmentId();

        //获取下属部门
        List<OrgModel> modelList = organizationMapper.selectOrgList(new OrgTreeSearch().setTenantId(ShieldContextHolder.getTenantId()));
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<Long> subOrgIdList = Lists.newArrayList();
        TreeModelUtil.findChildrenIds(departmentId, orgTreeModelList, subOrgIdList);
        List<Department> list = departmentMapper.selectByIds(subOrgIdList);

        //本部门
        Department department = departmentMapper.selectById(departmentId);
        list.add(department);
        log.info("用户{} {}部门数为{}", ShieldContextHolder.getUsername(), UserConstants.RolePermitCastType.subDepartment.getValue(), list.size());
        return list;
    }

    @Override
    public List<User> permissionUsers() {
        throw new UserCenterException("权限错误调用，请检查");
    }

    /**
     * 3 subDepartment
     * * 3.1 root找自己根部门
     * * 3.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 3.2.1 上级
     * * 找深度-1的上级部门 从t_org得到department_id（单个）
     * * 3.2.2 下级（本部门）
     * * 找深度+1的下级部门 从t_org得到department_id列表
     * * subDepartment 封装（true，false）-设置userNum为用户部门和用户所有与下级部门userNum（用户部门或用户上级部门）
     * * -设置userNum为当前部门和所有下级部门的userNum（用户下级部门）
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> organizationSimpleList(Long rootId) {
        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        List<DepartmentDTO> departmentDTOList;

        List<Department> departmentParamList = Lists.newArrayList();

        boolean isSup;
        if (rootId == null) {
            isSup = true;

            Organization organization = organizationMapper.selectRootPath(userDeptId);

            Department department = departmentMapper.selectByPrimaryKey(organization.getSuperiorId());

            departmentParamList.add(department);
        } else {
            Organization path = organizationMapper.selectOne(new Organization().setSuperiorId(rootId).setSubId(userDeptId));
            isSup = path != null;

            if (isSup) {
                Integer depth = path.getDepth() - 1;

                Organization organization = organizationMapper.selectOne(new Organization().setDepth(depth).setSubId(userDeptId));

                Department department = departmentMapper.selectByPrimaryKey(organization.getSuperiorId());

                departmentParamList.add(department);
            } else {
                List<Organization> organizationList = organizationMapper.selectSubList(Lists.newArrayList(rootId), 1);

                departmentParamList = departmentMapper.selectByIds(organizationList.stream().map(Organization::getSubId).collect(Collectors.toList()));
            }
        }

        departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, true, true);

        DepartmentDTO departmentDTO = departmentDTOList.get(0);

        Integer departmentUser;

        if (isSup) {
            departmentUser = departmentService.countRecursiveDepartmentUser(userDeptId);
        } else {
            departmentUser = departmentService.countRecursiveDepartmentUser(departmentDTO.getId());
        }

        departmentDTO.setUserNumber(departmentUser);

        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(String departmentName) {

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        List<Organization> selectSubList = organizationMapper.selectSubList(Lists.newArrayList(userDeptId), null);
        List<Long> subIdList = selectSubList.stream().map(Organization::getSubId).collect(Collectors.toList());

        List<Department> namedDepartmentList = departmentMapper.selectDepartmentList(new Department().setDepartmentName(departmentName));

        List<Department> finalNamedDepartmentList = Lists.newArrayList();
        for (Department department : namedDepartmentList) {
            if (subIdList.contains(department.getId())) {
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

        List<Long> departmentIdList = allDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        Map<Long, Integer> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

        List<OrgModel> modelList = allDepartmentList.stream().map(dep -> {
            OrgModel orgModel = new OrgModel();
            Organization organization = organizationMap.get(dep.getId());
            if (organization != null) {
                orgModel.setParentId(organization.getSuperiorId());
            }
            orgModel.setOrgId(dep.getId());
            orgModel.setOrgName(dep.getDepartmentName());
            orgModel.setTenantId(dep.getTenantId());
            Integer userNumber = recursiveDepartmentUserMap.get(dep.getId());
            orgModel.setUserNumber(userNumber);
            return orgModel;
        }).collect(Collectors.toList());

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).sorted(Comparator.comparing(OrgTreeModel::getOrder)).collect(Collectors.toList());

        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertClosure(orgTreeModelList, Lists.newArrayList(rootIds));

        return orgTree;
    }
}
