package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.converter.RoleConverter;
import cn.com.glsx.neshield.modules.entity.UserRoleRelation;
import cn.com.glsx.neshield.modules.model.RoleDTO;
import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.entity.RoleMenu;
import cn.com.glsx.neshield.modules.mapper.DepartmentMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMenuMapper;
import cn.com.glsx.neshield.modules.mapper.UserRoleRelationMapper;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private MenuService menuService;

    @Resource
    private DepartmentMapper departmentMapper;

    public R search(RoleSearch search) {

        PageHelper.startPage(search.getPageNumber(), search.getPageSize());

        List<Role> list = roleMapper.selectList(search);

        List<RoleDTO> roleDTOlist = getRoleListAssembled(list);

        PageInfo<RoleDTO> roleDTOPageInfo = new PageInfo<>(roleDTOlist);

        return R.ok().data(roleDTOPageInfo);
    }

    private List<RoleDTO> getRoleListAssembled(List<Role> list) {
        List<RoleDTO> roleDTOList = Lists.newArrayList();

        List<Long> departmentIds = Lists.newArrayList();

        list.forEach(role -> {
            String roleTenants = role.getRoleTenants();
            List<Long> roleTenantIdList = JSONArray.parseArray(roleTenants, Long.class);
            departmentIds.addAll(roleTenantIdList);
        });

        List<Department> departmentList = departmentMapper.selectByIds(departmentIds);

        Map<Long, Department> departmentMap = departmentList.stream().collect(Collectors.toMap(Department::getId, d -> d));

        List<RoleDTO> roleDtoList = list.stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);
            String roleTenants = role.getRoleTenants();
            List<Long> roleTenantIdList = JSONArray.parseArray(roleTenants, Long.class);

            List<Department> roleDepartmentList = Lists.newArrayList();
            for (Long departmentId : roleTenantIdList) {
                Department department = departmentMap.get(departmentId);
                roleDepartmentList.add(department);
            }

            roleDTO.setRoleDepartments(roleDepartmentList);

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
        return roleMapper.selectByPrimaryKey(roleId);
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
    public R simpleList() {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        return null;
    }

    @Transactional
    public R addRole(RoleBO roleBO) {
        Role role = RoleConverter.INSTANCE.boToDo(roleBO);

        List<Long> visibleTenant = roleBO.getVisibleTenant();

        role.setRoleTenants(JSON.toJSONString(visibleTenant));

        role.setContextInfo(true);

        long roleId = roleMapper.insertUseGeneratedKeys(role);

        List<Long> menuIdList = roleBO.getMenuIdList();
        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuId -> new RoleMenu(true).setMenuId(menuId)
                .setRoleId(roleId)).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);

        return R.ok();
    }

    @Transactional
    public R editRole(RoleBO roleBO) {
        Role role = RoleConverter.INSTANCE.boToDo(roleBO);

        List<Long> visibleTenant = roleBO.getVisibleTenant();

        role.setRoleTenants(JSON.toJSONString(visibleTenant));

        role.setContextInfo(false);

        roleMapper.updateByPrimaryKeySelective(role);

        Long id = role.getId();

        roleMenuMapper.logicDelByRoleId(id);

        List<Long> menuIdList = roleBO.getMenuIdList();
        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuId -> new RoleMenu(true).setMenuId(menuId)
                .setRoleId(id)).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);

        return R.ok();
    }


    public R roleInfo(Long id) {
        RoleDTO roleDTO = new RoleDTO();

        Role role = roleMapper.selectByPrimaryKey(id);
        if (role == null) {
            return R.ok();
        }

        BeanUtils.copyProperties(role, roleDTO);

        String roleTenants = role.getRoleTenants();
        List<Long> departmentIdList = JSONArray.parseArray(roleTenants, Long.class);

        List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

        roleDTO.setRoleDepartments(departmentList);

        return R.ok().data(roleDTO);
    }

    public R deleteRole(Long id) {
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setRoleId(id));

        if (CollectionUtils.isNotEmpty(userRoleRelations)){
            return R.error("请取消相关账号与该角色的关联后再删除");
        }

        roleMapper.logicDelete(id);

        return R.ok();
    }
}
