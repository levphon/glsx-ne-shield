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
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Slf4j
@Component
public class SubDepartmentStrategy extends PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    @Override
    public List<Department> permissionDepartments() {

        Long departmentId = ShieldContextHolder.getDepartmentId();

        List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(departmentId);

        List<Long> subOrgIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

        List<Department> list = departmentMapper.selectByIds(subOrgIdList);

        log.info("用户{} {}部门数为{}", ShieldContextHolder.getAccount(), UserConstants.RolePermitCastType.subDepartment.getValue(), list.size());
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
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        List<DepartmentDTO> departmentDTOList;

        List<Department> departmentParamList = Lists.newArrayList();

        if (rootId == null) {
            Department department = departmentMapper.selectById(userDeptId);
            departmentParamList.add(department);
        } else {
            List<Organization> organizationList = organizationMapper.selectSubOrgList(Lists.newArrayList(rootId), 1);

            if (CollectionUtils.isNotEmpty(organizationList)) {
                List<Long> departmentIdList = organizationList.stream().map(Organization::getSubId).collect(Collectors.toList());

                List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

                departmentParamList.addAll(departmentList);
            }
        }

        departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, true, true);

        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long deptId = ShieldContextHolder.getDepartmentId();

        search.setTenantId(tenantId);

        List<Department> subDepartmentList = this.permissionDepartments();

        List<Long> subDepartmentIdList = subDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        search.setOrgIds(subDepartmentIdList);

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<Long> departmentIdList = modelList.stream().map(OrgModel::getOrgId).collect(Collectors.toList());
        //计算用户数
        Map<Long, Integer> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<Long> subOrgIdList = Lists.newArrayList();
        TreeModelUtil.findChildrenIds(deptId, orgTreeModelList, subOrgIdList);
        subOrgIdList.add(deptId);

        orgTreeModelList.forEach(otm -> {
            if (subOrgIdList.contains(otm.getId())) {
                Integer number = recursiveDepartmentUserMap.get(otm.getId());
                otm.setUserNumber(number == null ? 0 : number);
            }
        });

        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByRootMark(orgTreeModelList, 1);

        return orgTree;
    }
}
