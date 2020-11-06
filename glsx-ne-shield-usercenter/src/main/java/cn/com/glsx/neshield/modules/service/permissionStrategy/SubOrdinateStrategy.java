package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.admin.common.constant.UserConstants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.mapper.UserMapper;
import cn.com.glsx.neshield.modules.mapper.UserPathMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class SubOrdinateStrategy extends PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserPathMapper userPathMapper;

    @Override
    public List<Department> permissionDepartments() {
        throw new UserCenterException("权限错误调用，请检查");
    }

    @Override
    public List<User> permissionUsers() {
        Long departmentId = ShieldContextHolder.getDepartmentId();
        Long userId = ShieldContextHolder.getUserId();

        //获取下属部门
        List<OrgModel> modelList = organizationMapper.selectOrgList(new OrgTreeSearch().setTenantId(ShieldContextHolder.getTenantId()));
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<Long> subOrgIdList = Lists.newArrayList();
        TreeModelUtil.findChildrenIds(departmentId, orgTreeModelList, subOrgIdList);
        subOrgIdList.add(departmentId);

        List<User> list = userMapper.selectDepartmentsSubordinate(new UserSearch().setUserId(userId).setDepartmentIdList(subOrgIdList));

        //本人
        User user = userMapper.selectById(userId);
        list.add(user);
        log.info("用户{} {}用户数为{}", ShieldContextHolder.getAccount(), UserConstants.RolePermitCastType.subordinate.getValue(), list.size());
        return list;
    }

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
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userId = ShieldContextHolder.getUserId();

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        List<Department> departmentParamList = Lists.newArrayList();

        if (rootId == null) {
            Department department = departmentMapper.selectById(userDeptId);

            departmentParamList.add(department);
        } else {
            //所有下级
            List<Organization> childrenOrgList = organizationMapper.selectList(new OrganizationBO()
                    .setSuperiorIdList(Lists.newArrayList(userDeptId))
                    .setBiggerDepth(1));
            List<Long> subIdList = childrenOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

            subIdList.add(userDeptId);

            if (subIdList.contains(rootId)) {
                List<Organization> subOrganizationList = organizationMapper.selectSubList(Lists.newArrayList(rootId), 1);

                List<Long> departmentIdList = subOrganizationList.stream().map(Organization::getSubId).collect(Collectors.toList());

                List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

                departmentParamList.addAll(departmentList);
            }
        }

        //存在用户的下级部门
        List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);
        List<Long> hasUserDeptIdList = departmentUserCountList.stream()
                .filter(duc -> duc.getDepartmentId() != null)
                .map(DepartmentUserCount::getDepartmentId)
                .collect(Collectors.toList());

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, false, false);
        for (DepartmentDTO departmentDTO : departmentDTOList) {
            if (rootId == null) {
                departmentDTO.setHasChildren(CollectionUtils.isNotEmpty(hasUserDeptIdList));
            } else {
                departmentDTO.setHasChildren(hasUserDeptIdList.contains(departmentDTO.getId()));
            }
        }
        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long userId = ShieldContextHolder.getUserId();

        List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);

        //下级部门id
        List<Long> subDepartmentIdList = departmentUserCountList.stream()
                .filter(duc -> duc.getDepartmentId() != null)
                .map(DepartmentUserCount::getDepartmentId)
                .collect(Collectors.toList());

        //模糊搜索得到的部门
        List<Department> namedDepartmentList = departmentMapper.selectDepartmentList(new Department()
                .setTenantId(tenantId)
                .setDepartmentName(search.getOrgName()));

        //提取符合搜索条件的部门
        List<Department> finalNamedDepartmentList = Lists.newArrayList();
        for (Department department : namedDepartmentList) {
            if (subDepartmentIdList.contains(department.getId())) {
                finalNamedDepartmentList.add(department);
            }
        }

        //符合条件的部门idlist
        List<Long> nameDepartmentIdList = finalNamedDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        List<Organization> superiorOrgList = organizationMapper.selectList(new OrganizationBO().setSubIdList(nameDepartmentIdList));
        //所有部门id
        List<Long> allDepartmentIdList = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        allDepartmentIdList.addAll(nameDepartmentIdList);

        List<Department> allDepartmentList = departmentMapper.selectByIds(allDepartmentIdList);
        //所有组织链
        List<Organization> organizationList = organizationMapper.selectList(new OrganizationBO().setSubIdList(allDepartmentIdList).setSuperiorIdList(allDepartmentIdList));
        //根节点
        List<Organization> rootList = organizationMapper.selectRootIdList(nameDepartmentIdList);

        Set<Long> rootIds = rootList.stream().map(Organization::getSuperiorId).collect(Collectors.toSet());

        Map<Long, Organization> organizationMap = organizationList.stream().filter(m -> m.getDepth() == 0 || m.getDepth() == 1).
                collect(Collectors.toMap(Organization::getSubId, treeModel -> treeModel));

        Map<Long, Integer> subordinateDepartmentUserCountMap = departmentUserCountList.stream().collect(Collectors.toMap(DepartmentUserCount::getDepartmentId, DepartmentUserCount::getUserNumber));

        List<OrgModel> modelList = allDepartmentList.stream().map(dep -> {
            OrgModel orgModel = new OrgModel();
            Organization org = organizationMap.get(dep.getId());
            if (org != null) {
                orgModel.setParentId(org.getSuperiorId());
            }
            orgModel.setOrgId(dep.getId());
            orgModel.setOrgName(dep.getDepartmentName());
            orgModel.setTenantId(dep.getTenantId());
            Integer userNumber = subordinateDepartmentUserCountMap.get(dep.getId());
            orgModel.setUserNumber(userNumber);
            orgModel.setOrderNum(dep.getOrderNum());
            return orgModel;
        }).collect(Collectors.toList());

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).sorted(Comparator.comparing(OrgTreeModel::getOrder)).collect(Collectors.toList());

        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertClosure(orgTreeModelList, Lists.newArrayList(rootIds));

        return orgTree;
    }
}
