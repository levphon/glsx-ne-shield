package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.DepartmentConverter;
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
import com.alibaba.fastjson.JSON;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.glsx.plat.exception.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
        tenant.setTenantName(orgBO.getDepartmentName());

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
        department.setDepartmentName(orgBO.getDepartmentName());
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
        department.setDepartmentName(organizationBO.getDepartmentName());
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

    @Transactional
    public void editOrganization(OrganizationBO orgBO) {
        Long organizationId = orgBO.getId();

        Department department = departmentMapper.selectById(organizationId);
        if (department == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "组织部门不存在");
        }
        department.setOrderNum(orgBO.getOrderNum());
        department.setDepartmentName(orgBO.getDepartmentName());
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
            tenant.setTenantName(orgBO.getDepartmentName());
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

        Integer userNum = userMapper.countByCriterial(new UserBO().setDepartmentIds(organizationIdList));
        if (userNum > 0) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该组织下仍关联有用户，请转移这部分用户后再重试");
        }

        //删除包括自己和下级所有部门
        departmentMapper.logicDeleteByIdList(organizationIdList);

        //删除自己下级所有路径
        organizationMapper.logicDeleteAllSubOrganization(organizationId);

        //删除自己对上级的所有路径
        organizationMapper.logicDeleteSelfBySubId(organizationId);

        //删除租户
        if (Constants.IS_ROOT_DEPARTMENT == department.getIsRoot()) {
            Long tenantId = department.getTenantId();
            tenantMapper.logicDeleteById(tenantId);
        }
    }

    public DepartmentDTO organizationInfo(Long organizationId) {
        DepartmentDTO departmentDTO = null;
        Department department = departmentMapper.selectById(organizationId);
        if (department != null) {
            departmentDTO = DepartmentConverter.INSTANCE.do2dto(department);
            if (department.getIsRoot() == Constants.IS_NOT_ROOT_DEPARTMENT) {
                Organization superiorOrg = organizationMapper.selectSuperiorOrganization(department.getId());
                if (superiorOrg != null) {
                    Department superiorDept = departmentMapper.selectById(superiorOrg.getSuperiorId());
                    departmentDTO.setSuperiorId(superiorDept.getId());
                    departmentDTO.setSuperiorName(superiorDept.getDepartmentName());
                }
            }
        }
        return departmentDTO;
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

        List<DepartmentDTO> departmentDTOList = permissionStrategy.orgSimpleList(rootId);

        return departmentDTOList;
    }

    /**
     * @param search
     * @return
     */
    public List<DepartmentDTO> permissionStrategy(OrgTreeSearch search) {

        Integer rolePermissionType = search.getRolePermissionType();

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

        List<DepartmentDTO> dtoList = permissionStrategy.orgSimpleList(search.getOrgId());
        log.info(JSON.toJSONString(dtoList, true));


        List list = permissionStrategy.orgTree(search);
        log.info(JSON.toJSONString(list, true));

        return dtoList;
    }

    public List orgTree(OrgTreeSearch search) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);
        if (StringUtils.isBlank(beanName)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限类型未知");
        }

        PermissionStrategy permissionStrategy = permissionStrategyMap.get(beanName);

        List<? extends TreeModel> treeModels = permissionStrategy.orgTree(search);

        return treeModels;
    }

}
