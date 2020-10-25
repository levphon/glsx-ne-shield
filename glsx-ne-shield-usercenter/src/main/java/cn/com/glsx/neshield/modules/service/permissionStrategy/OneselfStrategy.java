package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.mapper.UserMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
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
@Component
public class OneselfStrategy implements PermissionStrategy {

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Override
    public List<Department> permissionDepartments() {
        throw new UserCenterException("权限错误调用，请检查");
    }

    @Override
    public List<User> permissionUsers() {
        List<User> list = Lists.newArrayList();
        User user = userMapper.selectById(ShieldContextHolder.getUserId());
        CollectionUtils.addAll(list, user);
        return list;
    }

    /**
     * 2 self 或 selfDepartment
     * * 2.1 root 找自己根部门
     * * 2.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 2.2.1 上级
     * * 找确定深度的上级部门 先用rootId找到与自己部门的深度，-1得到确定深度，从t_org得到department_id（单个）
     * * 2.2.2 下级（本部门）
     * * 返回空
     * * 封装（false，false）-设置userNum为1（self）或设置userNum为自己部门人数（selfDepartment）-如果是本部门设置hasChild为false，非本部门设置hasChild为true
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> organizationSimpleList(Long rootId) {
        List<DepartmentDTO> departmentDTOList = Lists.newArrayList();

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        List<Department> departmentParamList = Lists.newArrayList();

        if (rootId == null) {
            Organization organization = organizationMapper.selectRootPath(userDeptId);

            Department department = departmentMapper.selectById(organization.getSuperiorId());

            departmentParamList.add(department);
        } else {
            Organization path = organizationMapper.selectOne(new Organization().setSuperiorId(rootId).setSubId(userDeptId));
            boolean isSup = path != null;

            if (isSup) {
                Integer depth = path.getDepth() - 1;

                Organization organization = organizationMapper.selectOne(new Organization().setDepth(depth).setSubId(userDeptId));

                Department department = departmentMapper.selectById(organization.getSuperiorId());

                departmentParamList.add(department);
            } else {
                return departmentDTOList;
            }
        }

        departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, false, false);

        departmentDTOList.forEach(dep -> dep.setUserNumber(1));

        departmentDTOList.forEach(dep -> {
            if (!dep.getId().equals(userDeptId)) {
                dep.setHasChildren(true);
            }
        });

        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(String departmentName) {

        List<? extends TreeModel> list = Lists.newArrayList();

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        Department selfDepartment = departmentMapper.selectByPrimaryKey(userDeptId);
        if (selfDepartment == null) {
            return list;
        }

        String selfDepartmentName = selfDepartment.getDepartmentName();
        if (!selfDepartmentName.contains(departmentName)) {
            return list;
        }

        List<Department> nameDepartmentList = Lists.newArrayList(selfDepartment);

        //符合条件的部门idlist
        List<Long> nameDepartmentIdList = nameDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

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

        List<OrgModel> modelList = allDepartmentList.stream().map(dep -> {
            OrgModel orgModel = new OrgModel();
            Organization organization = organizationMap.get(dep.getId());
            if (organization != null) {
                orgModel.setParentId(organization.getSuperiorId());
            }
            orgModel.setOrgId(dep.getId());
            orgModel.setOrgName(dep.getDepartmentName());
            orgModel.setTenantId(dep.getTenantId());
            orgModel.setUserNumber(1);
            return orgModel;
        }).collect(Collectors.toList());
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).sorted(Comparator.comparing(OrgTreeModel::getOrder)).collect(Collectors.toList());
        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertClosure(orgTreeModelList, Lists.newArrayList(rootIds));

        return orgTree;
    }
}
