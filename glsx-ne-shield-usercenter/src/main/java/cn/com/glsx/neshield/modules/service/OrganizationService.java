package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.service.permissionStrategy.PermissionStrategy;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Tenant;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.mapper.TenantMapper;
import cn.com.glsx.neshield.modules.mapper.UserMapper;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.all;
import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.getBeanNameByCode;

/**
 * @author: taoyr
 **/
@Slf4j
@Service
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentService departmentService;

    private static final int IS_ROOT_DEPARTMENT = 1;

    private static final int IS_NOT_ROOT_DEPARTMENT = 0;

    private final Map<String, PermissionStrategy> permissionStrategyMap;

    public OrganizationService(Map<String, PermissionStrategy> permissionStrategyMap) {
        this.permissionStrategyMap = permissionStrategyMap;
    }

    /**
     * 增加组织树节点
     *
     * @param organizationBO
     * @return
     */
    @Transactional
    public R addNodeToOrganization(OrganizationBO organizationBO) {
        Long rootId = organizationBO.getRootId();
        Department parentDepartment = departmentMapper.selectByPrimaryKey(rootId);
        if (parentDepartment == null || parentDepartment.getDelFlag() != 0) {
            return R.error("上级组织已删除，请刷新页面");
        }

        Long tenantId = parentDepartment.getTenantId();

        Department department = new Department(true);
        department.setTenantId(tenantId);
        department.setEnableStatus(organizationBO.getEnableStatus());
        department.setOrderNum(organizationBO.getOrderNum());
        department.setIsRoot(IS_NOT_ROOT_DEPARTMENT);
        department.setDepartmentName(organizationBO.getName());

        long departmentId = departmentMapper.insertUseGeneratedKeys(department);

        Organization organization = new Organization(true);
        organization.setSubId(departmentId);
        organization.setSuperiorId(rootId);
        organization.setTenantId(tenantId);
        int ret = organizationMapper.insertOrganizationPath(organization);
        if (ret > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 从组织树删除节点
     *
     * @param nodeId
     * @return
     */
    public int delNodeFromOrganization(Long nodeId) {
        try {
            //删除节点和子节点路径
            int ret = organizationMapper.deleteOrganizationPath(nodeId);
            if (ret < 1) {
                return 0;
            }
        } catch (Exception e) {
            log.error("从组织树删除节点异常", e);
            throw e;
        }
        return 1;
    }

    /**
     * 获取机构树
     *
     * @param search
     * @return
     */
    public List<? extends TreeModel> getOrgTree(OrgTreeSearch search) {
        List<OrgModel> modelList = organizationMapper.selectOrgList(search);
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByDepth(orgTreeModelList, 0);
        return orgTree;
    }

    public List<Long> getSuperiorIdsByName(String orgName) {
        List<String> idsStrs = organizationMapper.selectSuperiorIdsByName(orgName);

        List<Long> superiorIds = new ArrayList<>();
        idsStrs.forEach(idsStr -> {
            if (StringUtils.isNotEmpty(idsStr)) {
                String[] ids = idsStr.split(",");
                for (String id : ids) {
                    superiorIds.add(Long.valueOf(id));
                }
            }
        });
        return superiorIds;
    }

    /**
     * 插入根节点路径
     *
     * @param organizationBO
     * @return
     */
    @Transactional
    public R addRootOrganization(OrganizationBO organizationBO) {
        Long rootId = organizationBO.getRootId();
        if (rootId != null) {
            return R.error();
        }
        Tenant tenant = new Tenant(true);
        tenant.setTenantName(organizationBO.getName());

        Tenant duplicateNameTenant = tenantMapper.selectOne(tenant);
        if (duplicateNameTenant != null) {
            return R.error("已有同名的根节点");
        }

        long tenantId = tenantMapper.insertUseGeneratedKeys(tenant);

        Department department = new Department(true);
        department.setEnableStatus(organizationBO.getEnableStatus());
        department.setTenantId(tenantId);
        department.setIsRoot(IS_ROOT_DEPARTMENT);
        department.setOrderNum(organizationBO.getOrderNum());
        department.setDepartmentName(organizationBO.getName());

        long departmentId = departmentMapper.insertUseGeneratedKeys(department);

        Organization organization = new Organization(true);
        organization.setDepth(0);
        organization.setSuperiorId(departmentId);
        organization.setSubId(departmentId);
        organization.setTenantId(tenantId);
        int res = organizationMapper.insertRootPath(organization);

        if (res > 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @Transactional
    public R editOrganization(OrganizationBO organizationBO) {
        Long organizationId = organizationBO.getOrganizationId();

        Department department = departmentMapper.selectByPrimaryKey(organizationId);
        if (department == null) {
            return R.error();
        }

        department.setOrderNum(organizationBO.getOrderNum());
        department.setDepartmentName(organizationBO.getName());
        department.setEnableStatus(organizationBO.getEnableStatus());
        department.setUpdatedDate(new Date());
        department.setUpdatedBy(ShieldContextHolder.getUserId());
        departmentMapper.updateByPrimaryKeySelective(department);

        Tenant tenant = tenantMapper.selectByPrimaryKey(organizationId);
        if (tenant == null) {
            return R.ok();
        }

        tenant.setTenantName(organizationBO.getName());
        tenantMapper.updateByPrimaryKeySelective(tenant);

        return R.ok();
    }

    /**
     * 删除组织
     *
     * @param organizationId
     * @return
     */
    public R deleteOrganization(Long organizationId) {
        Department department = departmentMapper.selectByPrimaryKey(organizationId);
        if (department == null || department.getDelFlag() != 0) {
            return R.error("该组织不可用，请重新刷新列表");
        }

        List<Organization> organizations = organizationMapper.selectByRootId(organizationId);
        List<Long> organizationIdList = organizations.stream().map(Organization::getSubId).collect(Collectors.toList());

        int userNum = userMapper.countByCriterial(new UserBO().setDepartmentIds(organizationIdList));

        if (userNum > 0) {
            return R.error("该组织下仍关联有用户，请转移这部分用户后再重试");
        }

        //删除所有部门
        departmentMapper.logicDeleteByIdList(organizationIdList);

        //删除租户
        if (IS_ROOT_DEPARTMENT == department.getIsRoot()) {
            Long tenantId = department.getTenantId();
            tenantMapper.logicDeleteById(tenantId);
        }

        //删除所有路径
        organizationMapper.logicDeleteAllSubOrganization(organizationId);

        return R.ok();
    }

    /**
     * 查看节点是否为根节点
     *
     * @param organizationId
     * @return
     */
    public boolean isRootOrganization(Long organizationId) {
        Organization superiorOrganization = organizationMapper.selectSuperiorOrganization(organizationId);

        return superiorOrganization == null;
    }

    public R organizationInfo(Long organizationId) {
        Department department = departmentMapper.selectByPrimaryKey(organizationId);

        return R.ok().data(department);
    }

    public R childrenList(OrganizationSearch organizationSearch) {

        List<Department> departmentList = organizationMapper.selectChildrenList(organizationSearch);

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentList, true, false);

        if (ShieldContextHolder.isRoleAdmin() || all.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            return R.ok().data(departmentDTOList);
        }

        List<Department> currentUserDepartmentList = departmentService.getCurrentUserDepartment();
        List<Long> departmentIdList = currentUserDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        departmentDTOList = departmentDTOList.stream().filter(d -> departmentIdList.contains(d.getId())).collect(Collectors.toList());

        return R.ok().data(departmentDTOList);
    }

    /**
     * @param rootId
     * @return
     */
    public R simpleList(Long rootId) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);
        if (StringUtils.isBlank(beanName)) {
            return R.error("角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        List<DepartmentDTO> departmentDTOList = permissionStrategy.organizationSimpleList(rootId);

        return R.ok().data(departmentDTOList);
    }

    public R treeOrg(String departmentName) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);
        if (StringUtils.isBlank(beanName)) {
            return R.error("角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        List<? extends TreeModel> treeModels = permissionStrategy.orgTree(departmentName);

        return R.ok().data(treeModels);
    }
}
