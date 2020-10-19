package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
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
public class AllStrategy implements PermissionStrategy {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private DepartmentService departmentService;

    private static final int IS_ROOT_DEPARTMENT = 1;

    private static final int IS_NOT_ROOT_DEPARTMENT = 0;

    /**
     * 1 all
     * * 1.1 root 找所有根部门
     * * 1.2 非root 根据rootId找子部门list
     * * 封装-不过滤-返回
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> organizationSimpleList(Long rootId) {

        List<Department> departmentParamList;

        if (rootId == null) {
            departmentParamList = departmentMapper.selectDepartmentList(new Department().setIsRoot(IS_ROOT_DEPARTMENT));
        } else {
            departmentParamList = organizationMapper.selectChildrenList(new OrganizationSearch().setRootId(rootId));
        }

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, true, true);

        return departmentDTOList;
    }

    /**
     * 1.查询符合条件的部门nameDepartmentList
     * 2.找出idList所有向上的路径经过的所有部门allDepartmentIdList
     * 3.找出所有组织链organizationList
     * 4.找出根节点rootList
     * 5.封装-调用TreeModelUtil组装树
     *
     * @param departmentName
     * @return
     */
    @Override
    public List<? extends TreeModel> orgTree(String departmentName) {

        List<Department> nameDepartmentList = departmentMapper.selectDepartmentList(new Department().setDepartmentName(departmentName));
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

        List<Long> departmentIdList = allDepartmentList.stream().map(Department::getId).collect(Collectors.toList());
        Map<Long, Long> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

        List<OrgModel> modelList = allDepartmentList.stream().map(dep -> {
            OrgModel orgModel = new OrgModel();
            Organization organization = organizationMap.get(dep.getId());
            if (organization != null) {
                orgModel.setParentId(organization.getSuperiorId());
            }
            orgModel.setOrderNum(dep.getOrderNum());
            orgModel.setId(dep.getId());
            orgModel.setDeptName(dep.getDepartmentName());
            orgModel.setTenantId(dep.getTenantId());
            Long userNumber = recursiveDepartmentUserMap.get(dep.getId());
            orgModel.setUserNumber(userNumber);
            return orgModel;
        }).collect(Collectors.toList());
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).sorted(Comparator.comparing(OrgTreeModel::getOrderNum)).collect(Collectors.toList());
        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertClosure(orgTreeModelList, Lists.newArrayList(rootIds));

        return orgTree;
    }
}
