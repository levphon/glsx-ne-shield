package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.RoleConverter;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.entity.RoleMenu;
import cn.com.glsx.neshield.modules.entity.UserRoleRelation;
import cn.com.glsx.neshield.modules.mapper.RoleMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMenuMapper;
import cn.com.glsx.neshield.modules.mapper.UserRoleRelationMapper;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.com.glsx.neshield.modules.model.view.RoleDTO;
import cn.com.glsx.neshield.modules.model.view.SimpleRoleDTO;
import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.exception.SystemMessage;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RoleVisibility.*;

/**
 * @author: taoyr
 **/
@Slf4j
@Service
public class RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    public PageInfo<RoleDTO> search(RoleSearch search) {

        PageHelper.startPage(search.getPageNumber(), search.getPageSize());

        List<Role> list = roleMapper.selectList(search);

        List<RoleDTO> roleDTOList = getRoleListAssembled(list);

        return new PageInfo<>(roleDTOList);
    }

    private List<RoleDTO> getRoleListAssembled(List<Role> list) {

//        List<Long> departmentIdList = Lists.newArrayList();
//
//        list.forEach(role -> {
//            if (StringUtils.isNotEmpty(role.getRoleTenants())) {
//                String[] roleTenantsStr = role.getRoleTenants().split(",");
//                Long[] roleTenantIds = (Long[]) ConvertUtils.convert(roleTenantsStr, Long.class);
//                Collections.addAll(departmentIdList, roleTenantIds);
//            }
//        });

        List<RoleDTO> roleDtoList = list.stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);
            return roleDTO;
        }).collect(Collectors.toList());

        return roleDtoList;
    }

    public List<Role> getUserRoleList(Long userId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId(userId);
        List<UserRoleRelation> userRoleRelationList = userRoleRelationMapper.selectUserRoleRelationList(userRoleRelation);

        List<Long> roleIds = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        List<Role> roleList = roleMapper.selectRoleListByIds(roleIds);

        return roleList;
    }

    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    public int modifyRole(Role role) {
        try {
            if (role.getId() == null) {
                roleMapper.insert(role);
            } else {
                roleMapper.updateByPrimaryKeySelective(role);
            }
        } catch (Exception e) {
            log.error("编辑角色异常", e);
            throw e;
        }
        return 1;
    }

    /**
     * 根据角色权限范围获取角色列表
     *
     * @return
     */
    public List<SimpleRoleDTO> simpleList() {

        List<SimpleRoleDTO> list;

        List<Role> roleList;

        if (ShieldContextHolder.isSuperAdmin()) {
            //假设当前登陆的A账号（角色：系统管理员），则该账号创建账号的时候角色的选择范围可以覆盖系统所有角色
            roleList = roleMapper.selectList(new RoleSearch());
        } else {
            //假设当前登陆的B账号（角色：租户B管理员），则该账号创建账号的时候角色的选择范围可以覆盖共享角色+对该租户开放的角色
            roleList = Lists.newLinkedList();

            Integer roleVisibility = ShieldContextHolder.getRoleVisibility();
            if (onlyAdmin.getCode().equals(roleVisibility)) {
                roleList = roleMapper.selectList(new RoleSearch());
            } else if (share.getCode().equals(roleVisibility)) {
                roleList = roleMapper.selectByVisibilityType(roleVisibility);
            } else if (specifyTenants.getCode().equals(roleVisibility)) {
                String roleTenantsStr = ShieldContextHolder.getRole().getRoleTenants();

                Long[] roleTenantIds = (Long[]) ConvertUtils.convert(roleTenantsStr, Long.class);

                roleList = roleMapper.selectByTenantIds(Arrays.asList(roleTenantIds));
            }
        }

        list = roleList.stream().map(role -> new SimpleRoleDTO().setRoleId(role.getId()).setRoleName(role.getRoleName())).collect(Collectors.toList());

        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleBO roleBO) {

        checkRole(roleBO);

        Role role = RoleConverter.INSTANCE.bo2do(roleBO);

        role.setContextInfo(true);

        role.setTenantId(ShieldContextHolder.getRoleId());

        roleMapper.insertUseGeneratedKeys(role);

        List<Long> menuIdList = roleBO.getMenuIdList();

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuId -> new RoleMenu(true).setMenuId(menuId)
                .setRoleId(role.getId())).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editRole(RoleBO roleBO) {

        checkRole(roleBO);

        Role role = RoleConverter.INSTANCE.bo2do(roleBO);

        role.setContextInfo(false);

        roleMapper.updateByPrimaryKeySelective(role);

        Long id = role.getId();

        roleMenuMapper.logicDelByRoleId(id);

        List<Long> menuIdList = roleBO.getMenuIdList();

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuId -> new RoleMenu(true).setMenuId(menuId)
                .setRoleId(id)).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);
    }

    /**
     * 检查角色关键信息
     *
     * @param roleBO
     */
    private void checkRole(RoleBO roleBO) {
        if (roleBO.getRoleId() == null) {
            int cnt = roleMapper.selectCntByName(roleBO.getRoleName());
            if (cnt > 0) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同角色已存在");
            }
        } else {
            Role dbRole = roleMapper.selectByName(roleBO.getRoleName());
            if (dbRole != null && !dbRole.getId().equals(roleBO.getRoleId())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同角色已存在");
            }
        }
    }

    public RoleDTO roleInfo(Long id) {
        RoleDTO roleDTO = null;
        Role role = roleMapper.selectById(id);
        if (role != null) {
            roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);
        }
        return roleDTO;
    }

    public void deleteRole(Long id) {
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setRoleId(id));

        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "请取消相关账号与该角色的关联后再删除");
        }

        roleMapper.logicDeleteById(id);
    }
}
