package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.RoleConverter;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.entity.RoleMenu;
import cn.com.glsx.neshield.modules.entity.RoleTenant;
import cn.com.glsx.neshield.modules.entity.UserRoleRelation;
import cn.com.glsx.neshield.modules.mapper.RoleMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMenuMapper;
import cn.com.glsx.neshield.modules.mapper.RoleTenantMapper;
import cn.com.glsx.neshield.modules.mapper.UserRoleRelationMapper;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.com.glsx.neshield.modules.model.view.RoleDTO;
import cn.com.glsx.neshield.modules.model.view.SimpleRoleDTO;
import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.exception.SystemMessage;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    @Resource
    private RoleTenantMapper roleTenantMapper;

    public PageInfo<RoleDTO> search(RoleSearch search) {

        List<Role> list = Lists.newArrayList();

        Integer roleVisibility = ShieldContextHolder.getRoleVisibility();

        Page page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        if (onlyAdmin.getCode().equals(roleVisibility)) {
            //全部
            list = roleMapper.selectList(search);
        } else if (share.getCode().equals(roleVisibility)) {
            //共享
            list = roleMapper.selectList(search.setRoleVisibility(roleVisibility));
        } else if (specifyTenants.getCode().equals(roleVisibility)) {
            //指定租户+共享
            list = roleMapper.selectVisibilityList(search.setTenantId(ShieldContextHolder.getTenantId()));
        }

        List<RoleDTO> roleDTOList = getRoleListAssembled(list);

        PageInfo<RoleDTO> pageInfo = new PageInfo<>(roleDTOList);
        pageInfo.setPages(page.getPages());//总页数
        pageInfo.setTotal(page.getTotal());//总条数
        return pageInfo;
    }

    private List<RoleDTO> getRoleListAssembled(List<Role> list) {
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
            roleList = roleMapper.selectList(new RoleSearch().setEnableStatus(EnableStatus.enable.getCode()));
        } else {
            //假设当前登陆的B账号（角色：租户B管理员），则该账号创建账号的时候角色的选择范围可以覆盖共享角色+对该租户开放的角色
            roleList = Lists.newLinkedList();

            Integer roleVisibility = ShieldContextHolder.getRoleVisibility();
            if (onlyAdmin.getCode().equals(roleVisibility)) {
                roleList = roleMapper.selectList(new RoleSearch());
            } else if (share.getCode().equals(roleVisibility)) {
                roleList = roleMapper.selectByVisibilityType(roleVisibility);
            } else if (specifyTenants.getCode().equals(roleVisibility)) {
                List<Role> shareRoleList = roleMapper.selectByVisibilityType(share.getCode());

                List<Long> roleTenantIdList = roleTenantMapper.selectTenantIdsByRoleId(ShieldContextHolder.getRoleId());

                roleList = roleMapper.selectByTenantIds(roleTenantIdList);

                roleList.addAll(shareRoleList);
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

        role.setTenantId(ShieldContextHolder.getTenantId());

        roleMapper.insertUseGeneratedKeys(role);

        List<Long> menuIdList = roleBO.getMenuIdList();

        menuIdList = getFixMenuIds(menuIdList);

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuNo -> new RoleMenu(true)
                .setMenuNo(menuNo)
                .setRoleId(role.getId()))
                .collect(Collectors.toList());
        roleMenuMapper.insertList(roleMenuList);

        if (specifyTenants.getCode().equals(roleBO.getRoleVisibility())) {
            String[] tenantIds = roleBO.getRoleTenants().split(",");
            List<RoleTenant> roleTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                roleTenantList.add(new RoleTenant().setRoleId(role.getId()).setTenantId(Long.valueOf(item)));
            });
            roleTenantMapper.insertList(roleTenantList);
        }
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

        menuIdList = getFixMenuIds(menuIdList);

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuNo -> new RoleMenu(true)
                .setMenuNo(menuNo)
                .setRoleId(id)).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);

        roleTenantMapper.logicDeleteByRoleId(role.getId());
        if (specifyTenants.getCode().equals(roleBO.getRoleVisibility())) {
            String[] tenantIds = roleBO.getRoleTenants().split(",");
            List<RoleTenant> roleTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                roleTenantList.add(new RoleTenant().setRoleId(role.getId()).setTenantId(Long.valueOf(item)));
            });
            roleTenantMapper.insertList(roleTenantList);
        }
    }

    /**
     * 将缺失的夫级id补全
     *
     * @param menuIdList 页面传递的id集合
     */
    private List<Long> getFixMenuIds(List<Long> menuIdList) {
        //把已有的子菜单id转为Set
        HashSet<Long> parentids = new HashSet<>(menuIdList);

        //Long转String,根据01判断一级父id,每多一级长度加2,截取下一级父id,添加set
        menuIdList.stream().map(String::valueOf).forEach(menuId -> {
            int length = menuId.length();
            int rootIndex = menuId.indexOf("01") + 2;
            if (rootIndex != -1) {
                for (int i = rootIndex; i < length; i += 2) {
                    String parentIdStr = menuId.substring(0, i);
                    parentids.add(Long.parseLong(parentIdStr));
                }
            }
        });
        return new ArrayList<>(parentids);
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
        
        if (!ShieldContextHolder.isSuperAdmin() && onlyAdmin.getCode().equals(roleBO.getRoleVisibility())) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "非管理员账号不能创建管理员角色");
        }
    }

    public RoleDTO roleInfo(Long id) {
        RoleDTO roleDTO = null;
        Role role = roleMapper.selectById(id);
        if (role != null) {
            roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);

            if (specifyTenants.getCode().equals(role.getRoleVisibility())) {
                List<Long> roleTenantIdList = roleTenantMapper.selectTenantIdsByRoleId(id);
                String roleTenants = StringUtils.join(roleTenantIdList, ',');
                roleDTO.setRoleTenants(roleTenants);
            }
        }
        return roleDTO;
    }

    public void deleteRole(Long id) {
        List<UserRoleRelation> relations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setRoleId(id));
        if (CollectionUtils.isNotEmpty(relations)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "请取消相关账号与该角色的关联后再删除");
        }
        roleMapper.logicDeleteById(id);
    }
}
