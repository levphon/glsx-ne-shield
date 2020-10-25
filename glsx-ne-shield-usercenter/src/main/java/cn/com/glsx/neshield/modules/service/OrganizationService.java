package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Organization;
import cn.com.glsx.neshield.modules.entity.Tenant;
import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.OrganizationMapper;
import cn.com.glsx.neshield.modules.mapper.TenantMapper;
import cn.com.glsx.neshield.modules.mapper.UserMapper;
import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.OrgTreeModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.permissionStrategy.PermissionStrategy;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.glsx.plat.exception.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;

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

    private final Map<String, PermissionStrategy> permissionStrategyMap;

    public OrganizationService(Map<String, PermissionStrategy> permissionStrategyMap) {
        this.permissionStrategyMap = permissionStrategyMap;
    }

    /**
     * 插入根节点路径
     *
     * @param orgBO
     * @return
     */
    @Transactional
    public void addRootOrganization(OrganizationBO orgBO) {
        Tenant tenant = new Tenant(true);
        tenant.setTenantName(orgBO.getName());

        Tenant duplicateNameTenant = tenantMapper.selectOne(tenant);
        if (duplicateNameTenant != null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "已有同名的根节点");
        }
        tenantMapper.insertUseGeneratedKeys(tenant);

        Long tenantId = tenant.getId();

        Department department = new Department(true);
        department.setEnableStatus(orgBO.getEnableStatus());
        department.setTenantId(tenantId);
        department.setIsRoot(Constants.IS_ROOT_DEPARTMENT);
        department.setOrderNum(orgBO.getOrderNum());
        department.setDepartmentName(orgBO.getName());
        departmentMapper.insertUseGeneratedKeys(department);

        Long departmentId = department.getId();

        Organization organization = new Organization(true);
        organization.setSuperiorId(departmentId);
        organization.setSubId(departmentId);
        organization.setTenantId(tenantId);
        organizationMapper.insertRootPath(organization);
        log.info("新增根组织关系{}", organization.toString());
    }

    /**
     * 增加组织树节点
     *
     * @param organizationBO
     * @return
     */
    @Transactional
    public void addNodeToOrganization(OrganizationBO organizationBO) {
        Long superiorId = organizationBO.getSuperiorId();

        //选中的上级组织
        Department parentDept = departmentMapper.selectById(superiorId);
        if (parentDept == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "上级组织已删除，请刷新页面");
        }

        Long tenantId = parentDept.getTenantId();

        Department department = new Department(true);
        department.setTenantId(tenantId);
        department.setEnableStatus(organizationBO.getEnableStatus());
        department.setOrderNum(organizationBO.getOrderNum());
        department.setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);
        department.setDepartmentName(organizationBO.getName());
        departmentMapper.insertUseGeneratedKeys(department);

        Long departmentId = department.getId();

        Organization organization = new Organization(true);
        organization.setSuperiorId(superiorId);
        organization.setSubId(departmentId);
        organization.setTenantId(tenantId);
        int insertCnt = organizationMapper.insertOrganizationPath(organization);
        log.info("新增组织{}关系{}条", departmentId, insertCnt);
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
     * 获取组织机构树
     *
     * @param search
     * @return
     */
    public List<? extends TreeModel> fullOrgTree(OrgTreeSearch search) {
        List<OrgModel> modelList = organizationMapper.selectOrgList(search);
        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
        List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByDepth(orgTreeModelList, 0);
        return orgTree;
    }

    /**
     * 获取上级组织id
     *
     * @param orgName
     * @return
     */
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

    @Transactional
    public void editOrganization(OrganizationBO orgBO) {
        Long organizationId = orgBO.getOrganizationId();

        Department department = departmentMapper.selectById(organizationId);
        if (department == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "组织部门不存在");
        }
        department.setOrderNum(orgBO.getOrderNum());
        department.setDepartmentName(orgBO.getName());
        department.setEnableStatus(orgBO.getEnableStatus());
        department.setUpdatedDate(new Date());
        department.setUpdatedBy(ShieldContextHolder.getUserId());
        departmentMapper.updateByPrimaryKeySelective(department);

        //如果是根组织部门，则同时修改租户
        if (department.getIsRoot() == Constants.IS_ROOT_DEPARTMENT) {
            Tenant tenant = tenantMapper.selectById(department.getTenantId());
            if (tenant == null) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "组织租户不存在");
            }
            tenant.setTenantName(orgBO.getName());
            tenantMapper.updateByPrimaryKeySelective(tenant);
        }
    }

    /**
     * 删除组织
     *
     * @param organizationId
     * @return
     */
    public void deleteOrganization(Long organizationId) {
        Department department = departmentMapper.selectById(organizationId);
        if (department == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该组织不可用，请重新刷新列表");
        }

        List<Organization> organizations = organizationMapper.selectByRootId(organizationId);
        List<Long> organizationIdList = organizations.stream().map(Organization::getSubId).collect(Collectors.toList());

        int userNum = userMapper.countByCriterial(new UserBO().setDepartmentIds(organizationIdList));
        if (userNum > 0) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该组织下仍关联有用户，请转移这部分用户后再重试");
        }

        //删除所有部门
        departmentMapper.logicDeleteByIdList(organizationIdList);

        //删除租户
        if (Constants.IS_ROOT_DEPARTMENT == department.getIsRoot()) {
            Long tenantId = department.getTenantId();
            tenantMapper.logicDeleteById(tenantId);
        }

        //删除所有路径
        organizationMapper.logicDeleteAllSubOrganization(organizationId);

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

    public OrgModel organizationInfo(Long organizationId) {
        OrgModel orgModel = null;

        Department department = departmentMapper.selectByPrimaryKey(organizationId);
        if (department != null) {
            orgModel = new OrgModel();
            orgModel.setOrgId(department.getId());
            orgModel.setOrgName(department.getDepartmentName());
            orgModel.setTenantId(department.getTenantId());

            Tenant tenant = tenantMapper.selectByPrimaryKey(department.getTenantId());
            orgModel.setTenantName(tenant == null ? "" : tenant.getTenantName());
        }
        return orgModel;
    }

    public List<DepartmentDTO> childrenList(OrganizationSearch search) {

        List<Department> departmentList = organizationMapper.selectChildrenList(search);

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentList, true, false);

        if (ShieldContextHolder.isRoleAdmin() || all.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            return departmentDTOList;
        }

        List<Department> currentUserDepartmentList = departmentService.getCurrentUserDepartment();

        List<Long> departmentIdList = currentUserDepartmentList.stream().map(Department::getId).collect(Collectors.toList());

        departmentDTOList = departmentDTOList.stream().filter(d -> departmentIdList.contains(d.getId())).collect(Collectors.toList());

        return departmentDTOList;
    }

    /**
     * @param rootId
     * @return
     */
    public List<DepartmentDTO> simpleList(Long rootId) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);

        if (StringUtils.isBlank(beanName)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        List<DepartmentDTO> departmentDTOList = permissionStrategy.organizationSimpleList(rootId);

        return departmentDTOList;
    }

    /**
     * @param rolePermissionType
     * @return
     */
    public void permissionStrategy(Integer rolePermissionType) {

        String beanName = getBeanNameByCode(rolePermissionType);

        if (StringUtils.isBlank(beanName)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        if (oneself.getCode().equals(rolePermissionType)) {
            List<User> list = permissionStrategy.permissionUsers();
            log.info(String.valueOf(list.size()));
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            List<User> list = permissionStrategy.permissionUsers();
            log.info(String.valueOf(list.size()));
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            List<Department> list = permissionStrategy.permissionDepartments();
            log.info(String.valueOf(list.size()));
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            List<Department> list = permissionStrategy.permissionDepartments();
            log.info(String.valueOf(list.size()));
        }
    }

    public List orgTree(OrgTreeSearch search) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);
        if (StringUtils.isBlank(beanName)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        List<? extends TreeModel> treeModels = permissionStrategy.orgTree(search.getOrgName());

        return treeModels;
    }

}
