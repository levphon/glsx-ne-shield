package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.AuthDepartmentConverter;
import cn.com.glsx.neshield.modules.converter.AuthMenuPermissionConverter;
import cn.com.glsx.neshield.modules.converter.AuthRoleConverter;
import cn.com.glsx.neshield.modules.converter.AuthTenantConverter;
import cn.com.glsx.neshield.modules.entity.*;
import cn.com.glsx.neshield.modules.mapper.*;
import cn.com.glsx.neshield.modules.model.export.UserExport;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentCount;
import cn.com.glsx.neshield.modules.model.view.SuperTreeModel;
import cn.com.glsx.neshield.modules.model.view.UserDTO;
import cn.hutool.core.lang.UUID;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.exception.SystemMessage;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.jwt.util.ObjectUtils;
import com.glsx.plat.web.utils.SessionUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;
import static cn.com.glsx.admin.common.constant.UserConstants.RoleVisibility.*;

/**
 * @author liuyf
 * @desc 用户信息
 * @date 2019年10月24日 下午2:37:40
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private HashedCredentialsMatcher hcm;

    @Resource
    private JwtUtils<ComJwtUser> jwtUtils;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private UserPathMapper userPathMapper;

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    @Lazy
    private DepartmentService departmentService;

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByAccount(String username) {
        return userMapper.selectByAccount(username);
    }

    public UserDTO userInfo(Long userId) {
        UserDTO userDTO = null;

        User user = userMapper.selectById(userId);
        if (user != null) {
            userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setUserId(userId));
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                Long roleId = userRoleRelations.get(0).getRoleId();
                userDTO.setRoleId(roleId);

                Role role = roleMapper.selectById(roleId);
                userDTO.setRoleName(role != null ? role.getRoleName() : "");
            }

            Long departmentId = userDTO.getDepartmentId();
            Department department = departmentMapper.selectById(departmentId);
            if (department != null) {
                userDTO.setDepartmentName(department.getDepartmentName());
                userDTO.setDeptDepth(department.getIsRoot() == Constants.IS_ROOT_DEPARTMENT ? 0 : 1);
            }

            Long superiorId = userDTO.getSuperiorId();
            User superiorUser = userMapper.selectById(superiorId);
            userDTO.setSuperiorName(superiorUser != null ? superiorUser.getUsername() : "");
        }
        return userDTO;
    }

    public int logicDeleteById(Long id) {
        return userMapper.logicDeleteById(id);
    }

    public PageInfo<UserDTO> search(UserSearch search) {

        Page page = null;

        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        List<Long> searchDeptIdList = Lists.newArrayList();
        if (search.getDepartmentId() != null) {
            searchDeptIdList.add(search.getDepartmentId());
        }

        List<User> userList;

        if (oneself.getCode().equals(rolePermissionType)) {
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            userList = userMapper.selectList(search.setUserId(ShieldContextHolder.getUserId()));
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            searchDeptIdList.add(userDeptId);

            userList = userMapper.selectList(search.setDepartmentIdList(searchDeptIdList));
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            //所有子部门的下属员工
            List<Organization> subList = organizationMapper.selectSubList(Lists.newArrayList(userDeptId), null);

            List<Long> departmentIds = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

            departmentIds.add(userDeptId);

            departmentIds.addAll(searchDeptIdList);

            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
            userList = userMapper.selectDepartmentsSubordinate(search.setDepartmentIdList(departmentIds));
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            //所有属于用户下属部门的当前部门的子部门
            //用户下属部门
            List<Organization> userSubDepartmentList = organizationMapper.selectSubList(Lists.newArrayList(userDeptId), null);
            List<Long> userSubDepartmentIdList = userSubDepartmentList.stream().map(Organization::getSubId).collect(Collectors.toList());
            //当前部门的下属部门
            List<Organization> subList = organizationMapper.selectSubList(searchDeptIdList, null);
            List<Long> departmentIds = subList.stream().filter(dep -> userSubDepartmentIdList.contains(dep.getSubId())).map(Organization::getSubId).collect(Collectors.toList());

            departmentIds.add(userDeptId);

            departmentIds.addAll(searchDeptIdList);

            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
            userList = userMapper.selectList(new UserSearch().setDepartmentIdList(departmentIds));
        } else if (all.getCode().equals(rolePermissionType)) {
            //部门id
            if (CollectionUtils.isNotEmpty(searchDeptIdList)) {
                List<Organization> subList = organizationMapper.selectSubList(searchDeptIdList, null);
                List<Long> departmentIds = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

                departmentIds.addAll(searchDeptIdList);

                search.setDepartmentIdList(departmentIds);
            }
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
            userList = userMapper.selectList(search);
        } else {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "未知的角色权限范围类型");
        }
        List<UserDTO> userDTOList = Lists.newLinkedList();
        if (CollectionUtils.isNotEmpty(userList)) {
            userDTOList = userListAssembled(userList);
        }
        PageInfo<UserDTO> pageInfo = new PageInfo<>(userDTOList);
        pageInfo.setPages(page.getPages());//总页数
        pageInfo.setTotal(page.getTotal());//总条数
        return pageInfo;
    }

    public List<UserDTO> userListAssembled(List<User> userList) {
        List<UserDTO> userDTOList = Lists.newArrayList();

        List<Long> departmentIdList = userList.stream().map(User::getDepartmentId).collect(Collectors.toList());

        List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

        Map<Long, Department> departmentMap = departmentList.stream().collect(Collectors.toMap(Department::getId, d -> d));

        for (User user : userList) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            Department department = departmentMap.get(user.getDepartmentId());
            if (department != null) {
                userDTO.setDepartmentName(department.getDepartmentName());
            }
            userDTOList.add(userDTO);
        }

        return userDTOList;
    }

    public List<UserExport> export(UserSearch search) {
        return Lists.newArrayList();
    }

    public void addUser(UserBO userBO) {

        //检查用户关键信息
        checkUser(userBO);

        //校验部门范围
        checkUserDepartment(userBO);

        //校验上级范围
        checkUserSuperior(userBO);

        //校验角色
        checkUserRole(userBO);

        User user = new User(true);
        BeanUtils.copyProperties(userBO, user);
        generateAndSetPassword(user);
        userMapper.insertUseGeneratedKeys(user);

        Long userId = user.getId();

        if (userBO.getSuperiorId() == null) {
            UserPath userPath = new UserPath(true);
            userPath.setSuperiorId(userId);
            userPath.setSubId(userId);
            userPath.setTenantId(user.getTenantId());
            userPathMapper.insertRootPath(userPath);
            log.info("新增根用户关系{}", userPath.toString());
        } else {
            UserPath userPath = new UserPath(true);
            userPath.setSuperiorId(userBO.getSuperiorId());
            userPath.setSubId(userId);
            userPath.setTenantId(user.getTenantId());
            int insertCnt = userPathMapper.insertUserPath(userPath);
            log.info("新增用户{}关系{}条", userId, insertCnt);
        }

        userRoleRelationMapper.insert(new UserRoleRelation(true).setUserId(userId).setRoleId(userBO.getRoleId()));
    }

    public void editUser(UserBO userBO) {

        //检查用户关键信息
        checkUser(userBO);

        //校验部门范围
        checkUserDepartment(userBO);

        //校验上级范围
        checkUserSuperior(userBO);

        //校验角色
        checkUserRole(userBO);

        User user = userMapper.selectById(userBO.getId());
        BeanUtils.copyProperties(userBO, user);
        user.setDepartmentId(null);
        user.setTenantId(null);

        String password = user.getPassword();
        if (StringUtils.isNotBlank(password)) {
//            boolean regexPwd = RegexUtil.regexPwd(password);
//            if (!regexPwd) {
//                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "密码格式错误");
//            }
            generateAndSetPassword(user);
        }
        userMapper.updateByPrimaryKeySelective(user);

        //更新角色关系
        List<UserRoleRelation> relationList = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setUserId(user.getId()));
        if (CollectionUtils.isNotEmpty(relationList)) {
            UserRoleRelation relation = relationList.get(0);
            if (!relation.getRoleId().equals(userBO.getRoleId())) {
                relation.setRoleId(userBO.getRoleId());
                relation.setUpdatedBy(ShieldContextHolder.getUserId());
                relation.setUpdatedDate(new Date());
                userRoleRelationMapper.updateByPrimaryKeySelective(relation);
            }
        }
    }

    /**
     * 检查用户关键信息
     *
     * @param userBO
     */
    private void checkUser(UserBO userBO) {
        if (userBO.getId() == null) {
            int cnt = userMapper.selectCntByAccount(userBO.getAccount());
            if (cnt > 0) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同账号已存在");
            }
        } else {
            User dbUser = userMapper.selectByAccount(userBO.getAccount());
            if (dbUser != null && !dbUser.getId().equals(userBO.getId())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同账号已存在");
            }
        }
    }

    /**
     * 检查用户部门权限
     *
     * @param userBO
     */
    private void checkUserDepartment(UserBO userBO) {
        Long departmentId = userBO.getDepartmentId();
        List<Department> userDepartmentList = departmentService.getCurrentUserDepartment();
        List<Long> departmentIdList = userDepartmentList.stream().map(Department::getId).collect(Collectors.toList());
        if (!departmentIdList.contains(departmentId)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "当前用户不具备该部门权限");
        }
    }

    /**
     * 检查用户上级部门
     *
     * @param userBO
     */
    private void checkUserSuperior(UserBO userBO) {
        //上一级组织
        Organization superiorOrg = organizationMapper.selectSuperiorOrganization(userBO.getDepartmentId());
        if (superiorOrg != null) {
            User superiorUser = userMapper.selectById(userBO.getSuperiorId());
            if (superiorUser != null) {
                Long superiorUserDeptId = superiorUser.getDepartmentId();
                if (org.apache.commons.lang.ObjectUtils.notEqual(superiorUserDeptId, userBO.getDepartmentId()) &&
                        org.apache.commons.lang.ObjectUtils.notEqual(superiorUserDeptId, superiorOrg.getSuperiorId())) {
                    throw new UserCenterException(SystemMessage.FAILURE.getCode(), "上级用户范围不在本部门或上级部门");
                }
            }
            userBO.setTenantId(superiorOrg.getTenantId());
        } else {
            //根组织
            Department department = departmentMapper.selectById(userBO.getDepartmentId());
            userBO.setTenantId(department.getTenantId());
        }
    }

    /**
     * 检查用户角色权限
     *
     * @param userBO
     */
    private void checkUserRole(UserBO userBO) {
        boolean isAdmin = ShieldContextHolder.isRoleAdmin();
        if (!isAdmin) {
            Long roleId = userBO.getRoleId();

            Role role = roleMapper.selectById(roleId);
            if (onlyAdmin.getCode().equals(role.getRoleVisibility())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该角色不可选");
            } else if (share.getCode().equals(role.getRoleVisibility())) {

            } else if (specifyTenants.getCode().equals(role.getRoleVisibility())) {
                String[] roleTenantsStr = role.getRoleTenants().split(",");

                Long[] roleTenantIds = (Long[]) ConvertUtils.convert(roleTenantsStr, Long.class);

                Long rootDepartmentId = ShieldContextHolder.getTenantId();

                if (!Arrays.asList(roleTenantIds).contains(rootDepartmentId)) {
                    throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色不可选");
                }
            }
        }
    }

    /**
     * 生成密码
     *
     * @param user
     */
    private void generateAndSetPassword(User user) {
        String salt = StringUtils.generateRandomCode(false, 4);
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), user.getPassword(), salt, hcm.getHashIterations());
        user.setSalt(salt);
        user.setPassword(hash.toString());
    }

    /**
     * 验证密码
     *
     * @param user
     * @param inputPassword
     * @return
     */
    public boolean verifyPassword(User user, String inputPassword) {
        String dbPassword = user.getPassword();
        String salt = user.getSalt();
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), inputPassword, salt, hcm.getHashIterations());
        return dbPassword.equals(hash.toString());
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param password
     */
    public void changePassword(Long userId, String password) {

    }

    /**
     * 重置密码
     *
     * @param userId
     */
    public void resetPassword(Long userId) {

    }

    /**
     * 生成带用户信息的token
     *
     * @param user
     * @return
     */
    public String createToken(User user) {
        String uuid = UUID.randomUUID().toString(); //JWT 随机ID,做为验证的key
        String jwtId = jwtUtils.getApplication() + ":" + uuid + "_" + jwtUtils.JWT_SESSION_PREFIX + user.getId();
        ComJwtUser jwtUser = new ComJwtUser();
        jwtUser.setApplication(jwtUtils.getApplication());
        jwtUser.setJwtId(jwtId);
        jwtUser.setUserId(String.valueOf(user.getId()));
        jwtUser.setAccount(user.getAccount());

        Department department = departmentMapper.selectById(user.getDepartmentId());
        jwtUser.setBelong(department == null ? "" : department.getDepartmentName());

        Map<String, Object> userMap = (Map<String, Object>) ObjectUtils.objectToMap(jwtUser);

        return jwtUtils.createToken(userMap);
    }

    /**
     * 根据Token获取Customer
     */
    public User getByToken() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNullOrEmpty(token)) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        Map<String, Object> userMap = jwtUtils.parseClaim(token);
        Object userId = userMap.get("userId");
        if (Objects.isNull(userId)) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        User user = this.getById(Long.valueOf(userId.toString()));
        if (user == null) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        return user;
    }

    /**
     * 从redis删除token缓存
     */
    public void removeToken() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        jwtUtils.destroyToken(token);
    }

    /**
     * 获取综合用户信息
     *
     * @return
     */
    public SyntheticUser getSyntheticUser() {
        User user = this.getByToken();

        SyntheticUser authUser = new SyntheticUser();
        authUser.setUserId(user.getId());
        authUser.setAccount(user.getAccount());
        authUser.setAdmin(user.isAdmin());

        // TODO: 2020/9/24 缓存数据


        //租户
        Tenant tenant = tenantMapper.selectById(user.getTenantId());
        authUser.setTenant(AuthTenantConverter.INSTANCE.toAuthTenant(tenant));

        //部门
        Department department = departmentMapper.selectById(user.getDepartmentId());
        authUser.setDepartment(AuthDepartmentConverter.INSTANCE.toAuthDepartment(department));

        //角色
        List<Role> roleList = roleService.getUserRoleList(user.getId());
        if (CollectionUtils.isNotEmpty(roleList)) {
            List<cn.com.glsx.auth.model.Role> list = new ArrayList<>(roleList.size());
            roleList.forEach(role -> {
                list.add(AuthRoleConverter.INSTANCE.toAuthRole(role));
            });
            authUser.setRoleList(list);
        }
        //菜单
        if (CollectionUtils.isNotEmpty(authUser.getRoleList())) {
            List<Long> roleIds = authUser.getRoleList().stream().map(cn.com.glsx.auth.model.Role::getRoleId).collect(Collectors.toList());

//            List<Menu> menuList = menuService.getMenuList(roleIds);
//            List<cn.com.glsx.auth.model.Menu> list = new ArrayList<>(roleList.size());
//            menuList.forEach(menu -> {
//                list.add(AuthMenuConverter.INSTANCE.toAuthMentu(menu));
//            });

            List<Long> menuIdList = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
            List<MenuPermission> permissionList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(menuIdList)) {
                permissionList = menuService.getMenuPermissions(menuIdList);
            }
            List<cn.com.glsx.auth.model.MenuPermission> list = new ArrayList<>(permissionList.size());
            permissionList.forEach(mp -> {
                list.add(AuthMenuPermissionConverter.INSTANCE.toAuthMenuPermission(mp));
            });
            authUser.setMenuPermissionList(list);
        }

        //可见用户
//        authUser.setOwnerIdList(this.getRelationAuthUserIds());

        return authUser;
    }

    public List<DepartmentCount> countDepartmentUserNumber(List<Long> departmentIdList) {
        return userMapper.countDepartmentsUser(departmentIdList);
    }

    public List<Long> getRelationAuthUserIds() {
        SyntheticUser user = this.getSyntheticUser();

        Long userId = user.getUserId();

        Long departmentId = user.getDepartment().getDeptId();

        List<Long> userIds = Lists.newArrayList();

        Integer rolePermissionType = user.getRoleList().get(0).getRolePermissionType();

        String beanName = getBeanNameByCode(rolePermissionType);

        if (StringUtils.isBlank(beanName)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限类型未知");
        }

        if (oneself.getCode().equals(rolePermissionType)) {
            userIds.add(userId);
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            List<User> userList = userMapper.selectList(new UserSearch().setDepartmentIdList(Lists.newArrayList(departmentId)));

            userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            List<Organization> subOrgList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);

            List<Long> departmentIds = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

            departmentIds.add(departmentId);

            List<User> userList = userMapper.selectList(new UserSearch().setDepartmentIdList(departmentIds));

            userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            List<UserPath> userPaths = userPathMapper.selectUserSubordinateList(userId);

            userIds = userPaths.stream().map(UserPath::getSubId).collect(Collectors.toList());

            userIds.add(userId);
        } else if (all.getCode().equals(rolePermissionType)) {
//            do nothing
        } else {
            throw new UserCenterException("未知角色权限查询类型");
        }
        return userIds;
    }

    public List<SuperTreeModel> suitableSuperUsers(Long departmentId) {
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new UserCenterException("部门不存在");
        }
        List<User> departmentUserList = userMapper.select(new User().setDepartmentId(departmentId));

        Organization superiorOrganization = organizationMapper.selectSuperiorOrganization(departmentId);
        if (superiorOrganization == null) {
            //无上级
            SuperTreeModel superTreeModel = new SuperTreeModel();
            superTreeModel.setId(departmentId);
            superTreeModel.setLabel(department.getDepartmentName());
            superTreeModel.setOrder(department.getOrderNum());
            superTreeModel.setRoot(true);
            superTreeModel.setDisabled(true);

            List<SuperTreeModel> children = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

            superTreeModel.setChildren(children);

            return Lists.newArrayList(superTreeModel);
        }

        Long superiorId = superiorOrganization.getSuperiorId();
        Department superiorDepartment = departmentMapper.selectById(superiorId);

        SuperTreeModel superTreeModel = new SuperTreeModel();
        superTreeModel.setId(superiorDepartment.getId());
        superTreeModel.setLabel(superiorDepartment.getDepartmentName());
        superTreeModel.setOrder(superiorDepartment.getOrderNum());

        List<User> superiorUserList = userMapper.select(new User().setDepartmentId(superiorId));

        List<SuperTreeModel> children = superiorUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        SuperTreeModel departmentModel = new SuperTreeModel();
        departmentModel.setId(departmentId);
        departmentModel.setLabel(department.getDepartmentName());
        departmentModel.setOrder(department.getOrderNum());
        superTreeModel.setDisabled(true);

        children.add(departmentModel);

        superTreeModel.setChildren(children);

        List<SuperTreeModel> departmentUserModels = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        departmentModel.setChildren(departmentUserModels);

        return Lists.newArrayList(superTreeModel);
    }

    private static SuperTreeModel applySuperTreeModel(User du) {
        SuperTreeModel model = new SuperTreeModel();
        model.setId(du.getId());
        model.setLabel(du.getUsername());
        return model;
    }

}
