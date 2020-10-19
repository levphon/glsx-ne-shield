package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.util.RegexUtil;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.AuthMenuConverter;
import cn.com.glsx.neshield.modules.converter.AuthRoleConverter;
import cn.com.glsx.neshield.modules.converter.DepartmentConverter;
import cn.com.glsx.neshield.modules.entity.*;
import cn.com.glsx.neshield.modules.mapper.*;
import cn.com.glsx.neshield.modules.model.UserDTO;
import cn.com.glsx.neshield.modules.model.export.UserExport;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentCount;
import cn.com.glsx.neshield.modules.model.view.SuperTreeModel;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.core.constant.BasicConstants;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.SystemMessage;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.jwt.util.ObjectUtils;
import com.glsx.plat.web.utils.SessionUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;
import static cn.com.glsx.admin.common.constant.UserConstants.roleVisibility.onlyAdmin;
import static cn.com.glsx.admin.common.constant.UserConstants.roleVisibility.specifyTenants;

/**
 * @author liuyf
 * @desc 用户信息
 * @date 2019年10月24日 下午2:37:40
 */
@Service
public class UserService {

    @Resource
    private HashedCredentialsMatcher hcm;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserMapper userMapper;

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
    @Lazy
    private DepartmentService departmentService;

    public User getById(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    public User findByAccount(String username) {
        return userMapper.selectByAccount(username);
    }

    public UserDTO userInfo(Long userId) {
        UserDTO userDTO = new UserDTO();

        User user = userMapper.selectByPrimaryKey(userId);

        BeanUtils.copyProperties(user, userDTO);

        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setUserId(userId));
        if (CollectionUtils.isNotEmpty(userRoleRelations) && userRoleRelations.get(0) != null) {
            Long roleId = userRoleRelations.get(0).getId();

            Role role = roleMapper.selectByPrimaryKey(roleId);

            userDTO.setRoleName(role.getRoleName());
        }

        Long departmentId = userDTO.getDepartmentId();
        Department department = departmentMapper.selectByPrimaryKey(departmentId);
        userDTO.setDepartmentName(department != null ? department.getDepartmentName() : "");

        Long superiorId = userDTO.getSuperiorId();
        User superiorUser = userMapper.selectByPrimaryKey(superiorId);
        userDTO.setSuperiorName(superiorUser != null ? superiorUser.getUsername() : "");

        return userDTO;
    }

    public int logicDeleteById(Long id) {
        return userMapper.logicDeleteById(id);
    }

    public R search(UserSearch search) {
        PageInfo<UserDTO> userDTOPageInfo = new PageInfo<>();

        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        Long userId = ShieldContextHolder.getUserId();

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        Long departmentId = search.getDepartmentId();

        List<User> userList;

        if (oneself.getCode().equals(rolePermissionType)) {
            PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            userList = userMapper.selectList(search.setUserId(userId));
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            search.setDepartmentId(userDeptId);

            PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            userList = userMapper.selectList(search);
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            //所有子部门的下属员工
            List<Organization> subList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);

            List<Long> departmentIds = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

            PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            userList = userMapper.selectDepartmentsSubordinate(departmentIds, userId, search);
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            //所有属于用户下属部门的当前部门的子部门
            //用户下属部门
            List<Organization> userSubDepartmentList = organizationMapper.selectSubList(Lists.newArrayList(userDeptId), null);
            List<Long> userSubDepartmentIdList = userSubDepartmentList.stream().map(Organization::getSubId).collect(Collectors.toList());

            //当前部门的下属部门
            List<Organization> subList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);
            List<Long> departmentIdList = subList.stream().filter(dep -> userSubDepartmentIdList.contains(dep.getSubId())).map(Organization::getSubId).collect(Collectors.toList());

            userList = userMapper.selectList(new UserSearch().setDepartmentIdList(departmentIdList));
        } else if (all.getCode().equals(rolePermissionType)) {
            List<Organization> subList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);

            List<Long> departmentIdList = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

            userList = userMapper.selectList(search.setDepartmentIdList(departmentIdList));
        } else {
            return R.error("未知的角色权限范围类型");
        }

        List<UserDTO> userDTOList = userListAssembled(userList);

        userDTOPageInfo.setList(userDTOList);

        return R.ok().putPageData(userDTOPageInfo);
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

    public R addUser(UserBO userBO) {

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

        userMapper.insertSelective(user);

        return R.ok();
    }

    public R editUser(UserBO userBO) {

        //检查用户关键信息
        checkUser(userBO);

        //校验部门范围
        checkUserDepartment(userBO);

        //校验上级范围
        checkUserSuperior(userBO);

        //校验角色
        checkUserRole(userBO);

        User user = new User(false);
        BeanUtils.copyProperties(userBO, user);
        user.setDepartmentId(null);
        user.setTenantId(null);

        String password = user.getPassword();
        if (StringUtils.isNotBlank(password)) {
            boolean regexPwd = RegexUtil.regexPwd(password);
            if (!regexPwd) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "密码格式错误");
            }
            generateAndSetPassword(user);
        }

        userMapper.updateByPrimaryKeySelective(user);
        return R.ok();
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
     * 检查用户上级
     *
     * @param userBO
     */
    private void checkUserSuperior(UserBO userBO) {
        Organization superiorOrg = organizationMapper.selectSuperiorOrganization(userBO.getDepartmentId());
        Long superiorId = userBO.getSuperiorId();
        if (superiorId != null) {
            User superiorUser = userMapper.selectByPrimaryKey(superiorId);
            if (superiorUser == null) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "上级用户不存在");
            }
            Long superiorUserDepartmentId = superiorUser.getDepartmentId();
            if (org.apache.commons.lang.ObjectUtils.notEqual(superiorUserDepartmentId, userBO.getDepartmentId()) && (superiorOrg == null ||
                    org.apache.commons.lang.ObjectUtils.notEqual(superiorUserDepartmentId, superiorOrg.getSuperiorId()))) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "上级用户范围不在本部门或上级部门");
            }
        }
        userBO.setTenantId(superiorOrg.getTenantId());
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

            Role role = roleMapper.selectByPrimaryKey(roleId);
            if (onlyAdmin.getCode().equals(role.getRoleVisibility())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该角色不可选");
            }
            if (specifyTenants.getCode().equals(role.getRoleVisibility())) {
                String roleTenants = role.getRoleTenants();

                List<Long> rootDepartmentIdList = JSONArray.parseArray(roleTenants, Long.class);

                Long rootDepartmentId = ShieldContextHolder.getRootDepartmentId();

                if (!rootDepartmentIdList.contains(rootDepartmentId)) {
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

    public String createToken(User user) {
        String jwtId = UUID.randomUUID().toString(); //JWT 随机ID,做为验证的key
        ComJwtUser jwtUser = new ComJwtUser();
        jwtUser.setJwtId(jwtId);
        jwtUser.setUserId(user.getId());
        jwtUser.setAccount(user.getAccount());
        jwtUser.setDeptId(user.getDepartmentId());

        Department department = departmentMapper.selectByPrimaryKey(user.getDepartmentId());
        jwtUser.setBelong(department == null ? "" : department.getDepartmentName());

        Map<String, Object> userMap = (Map<String, Object>) ObjectUtils.objectToMap(jwtUser);

        return jwtUtils.createToken(jwtId, userMap);
    }

    /**
     * 根据Token获取Customer
     */
    public User getByToken() {
        String token = SessionUtils.request().getHeader(BasicConstants.REQUEST_HEADERS_TOKEN);

        if (StringUtils.isNullOrEmpty(token)) {
            throw new UserCenterException(SystemMessage.ILLEGAL_ACCESS.getCode(), "登录已失效");
        }

        //解析token，反转成JwtUser对象
        Map<String, Object> userMap = jwtUtils.parseClaim(token, ComJwtUser.class);
        ComJwtUser jwtUser = null;
        try {
            jwtUser = (ComJwtUser) ObjectUtils.mapToObject(userMap, ComJwtUser.class);
        } catch (Exception e) {
            throw new UserCenterException(SystemMessage.ILLEGAL_ACCESS.getCode(), "登录已失效");
        }
        User user = this.getById(jwtUser.getUserId());
        if (user == null || !SysConstants.DeleteStatus.normal.getCode().equals(user.getDelFlag())) {
            throw new UserCenterException(SystemMessage.ILLEGAL_ACCESS.getCode(), SystemMessage.ILLEGAL_ACCESS.getMsg());
        }
        return user;
    }

    public SyntheticUser getSyntheticUser() {
        User user = this.getByToken();

        SyntheticUser authUser = new SyntheticUser();
        authUser.setUserId(user.getId());
        authUser.setUsername(user.getAccount());

        // TODO: 2020/9/24 缓存数据

        //部门
        Department department = departmentMapper.selectByPrimaryKey(user.getDepartmentId());
        authUser.setDepartment(DepartmentConverter.INSTANCE.toAuthDepartment(department));

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

            List<Menu> menuList = menuService.getMenuList(roleIds);
            List<cn.com.glsx.auth.model.Menu> list = new ArrayList<>(roleList.size());
            menuList.forEach(menu -> {
                list.add(AuthMenuConverter.INSTANCE.toAuthMentu(menu));
            });
            authUser.setMenuList(list);
        }
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
        if (oneself.getCode().equals(rolePermissionType)) {
            userIds.add(userId);
        } else if (selfDepartment.getCode().equals(rolePermissionType)) {
            List<User> userList = userMapper.selectList(new UserSearch().setDepartmentId(departmentId));

            userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        } else if (subDepartment.getCode().equals(rolePermissionType)) {
            List<Organization> subOrgList = organizationMapper.selectSubList(Lists.newArrayList(departmentId), null);
            List<Long> departmentIds = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

            List<User> userList = userMapper.selectList(new UserSearch().setDepartmentIdList(departmentIds));

            userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        } else if (subordinate.getCode().equals(rolePermissionType)) {
            List<UserPath> userPaths = userPathMapper.selectUserSubordinateList(userId);

            userIds = userPaths.stream().map(UserPath::getSubId).collect(Collectors.toList());
        } else if (all.getCode().equals(rolePermissionType)) {
            throw new UserCenterException("全表查询用户，太多啦");
        } else {
            throw new UserCenterException("未知角色权限查询类型");
        }

        return userIds;
    }

    public R suitableSuperUsers(Long departmentId) {
        SuperTreeModel superTreeModel = new SuperTreeModel();

        Department department = departmentMapper.selectByPrimaryKey(departmentId);
        if (department == null) {
            return R.error("部门不存在");
        }
        User userParam = new User().setDepartmentId(departmentId);
        userParam.setDelFlag(0);
        List<User> departmentUserList = userMapper.select(userParam);

        Organization superiorOrganization = organizationMapper.selectSuperiorOrganization(departmentId);
        if (superiorOrganization == null) {
            //无上级
            superTreeModel.setId(departmentId);
            superTreeModel.setLabel(department.getDepartmentName());
            superTreeModel.setOrder(department.getOrderNum());

            List<SuperTreeModel> children = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

            superTreeModel.setChildren(children);

            return R.ok().data(superTreeModel);
        }

        Long superiorId = superiorOrganization.getSuperiorId();
        Department superiorDepartment = departmentMapper.selectByPrimaryKey(superiorId);
        superTreeModel.setId(superiorDepartment.getId());
        superTreeModel.setLabel(superiorDepartment.getDepartmentName());
        superTreeModel.setOrder(superiorDepartment.getOrderNum());

        userParam.setDepartmentId(superiorId);
        List<User> superiorUserList = userMapper.select(userParam);

        List<SuperTreeModel> children = superiorUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        SuperTreeModel departmentModel = new SuperTreeModel();
        departmentModel.setId(departmentId);
        departmentModel.setLabel(department.getDepartmentName());
        departmentModel.setOrder(department.getOrderNum());

        children.add(departmentModel);

        superTreeModel.setChildren(children);

        List<SuperTreeModel> departmentUserModels = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        departmentModel.setChildren(departmentUserModels);

        return R.ok().data(superTreeModel);
    }

    private static SuperTreeModel applySuperTreeModel(User du) {
        SuperTreeModel model = new SuperTreeModel();
        model.setId(du.getId());
        model.setLabel(du.getUsername());
        return model;
    }

}
