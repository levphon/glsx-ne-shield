package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.admin.common.enums.MenuType;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.MenuConverter;
import cn.com.glsx.neshield.modules.entity.Menu;
import cn.com.glsx.neshield.modules.entity.MenuPermission;
import cn.com.glsx.neshield.modules.entity.RoleMenu;
import cn.com.glsx.neshield.modules.mapper.MenuMapper;
import cn.com.glsx.neshield.modules.mapper.MenuPermissionMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMenuMapper;
import cn.com.glsx.neshield.modules.model.MenuModel;
import cn.com.glsx.neshield.modules.model.MenuTreeModel;
import cn.com.glsx.neshield.modules.model.export.MenuExport;
import cn.com.glsx.neshield.modules.model.param.MenuBO;
import cn.com.glsx.neshield.modules.model.param.MenuSearch;
import cn.com.glsx.neshield.modules.model.param.MenuTreeSearch;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.model.view.MenuDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.glsx.plat.redis.utils.RedisUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.RedisConstants.FIVE_SECONDS;
import static cn.com.glsx.admin.common.constant.RedisConstants.MENU_NO_LOCK;

/**
 * @author: taoyr
 **/
@Slf4j
@Service
public class MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private MenuPermissionMapper menuPermissionMapper;

    @Autowired
    private RedisUtils redisUtils;

    public PageInfo<MenuDTO> search(MenuSearch search) {
        Page page = null;
        //查子菜单数据不分页
        if (Objects.isNull(search.getParentId())) {
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        } else {
            page = PageHelper.startPage(1, Short.MAX_VALUE);
        }
        List<MenuDTO> menuDTOList = menuMapper.selectDTOList(search);
        menuDTOList.forEach(m -> {
            int cnt = menuMapper.selectChildrenCntByParentId(m.getMenuNo());
            m.setHasChildren(cnt > 0);

            setParentMenuInfo(m);
        });
        PageInfo<MenuDTO> pageInfo = new PageInfo<>(menuDTOList);
        pageInfo.setPages(page.getPages());//总页数
        pageInfo.setTotal(page.getTotal());//总条数
        return pageInfo;
    }

    public List<MenuExport> export(UserSearch search) {
        return Lists.newArrayList();
    }

    public List<MenuDTO> children(Long parentId) {
        List<Menu> menuList = menuMapper.selectByParentId(parentId);
        List<MenuDTO> list = new ArrayList<>(menuList.size());
        for (Menu menu : menuList) {
            MenuDTO menuDTO = MenuConverter.INSTANCE.do2dto(menu);

            int cnt = menuMapper.selectChildrenCntByParentId(menuDTO.getMenuNo());
            menuDTO.setHasChildren(cnt > 0);

            setParentMenuInfo(menuDTO);

            list.add(menuDTO);
        }
        return list;
    }

    private void setParentMenuInfo(MenuDTO menuDTO) {
        if (menuDTO.getParentId() != 0L) {
            Menu pMenu = menuMapper.selectByMenuNo(menuDTO.getParentId());
            if (pMenu != null) {
                menuDTO.setPpId(pMenu.getId());
                menuDTO.setParentName(pMenu.getMenuName());
            }
        }
    }

    /**
     * 获取菜单树-全部菜单
     *
     * @param roleIds
     * @return
     */
    public List<MenuModel> getMenuFullTreeWithChecked(List<Long> roleIds, Long editRoleId) {
        List<MenuModel> modelList = Lists.newArrayList();
        //当前登录用户所拥有的权限
        if (ShieldContextHolder.isSuperAdmin()) {
            modelList = menuMapper.selectMenuFullTree();
        } else {
            modelList = menuMapper.selectMenuPermTree(new MenuTreeSearch().setRoleIds(roleIds).setMenuTypes(MenuType.getAllTypes()));
        }
        //被编辑角色的权限,editRoleId==null为新增角色时查询
        if (editRoleId != null) {
            List<MenuModel> permMenuList = menuMapper.selectMenuPermTree(new MenuTreeSearch().setRoleIds(Lists.newArrayList(editRoleId)).setMenuTypes(MenuType.getAllTypes()));
            modelList.forEach(mm -> {
                for (MenuModel mm2 : permMenuList) {
                    if (mm.getMenuNo().equals(mm2.getMenuNo())) {
                        mm.setChecked(true);
                        break;
                    }
                }
            });
        }
        List<MenuTreeModel> menuTreeModelList = modelList.stream().map(MenuTreeModel::new).collect(Collectors.toList());
        List menuTree = TreeModelUtil.fastConvertByRootId(menuTreeModelList, 0L);
        Collections.sort(menuTree, Comparator.comparing(MenuTreeModel::getOrder));
        return menuTree;
    }

    public Set<Long> getMenuCheckedIds(Long editRoleId) {
        List<MenuModel> permMenuList = menuMapper.selectMenuPermTree(new MenuTreeSearch().setRoleIds(Lists.newArrayList(editRoleId)).setMenuTypes(MenuType.getAllTypes()));
        Set<Long> permMenuIds = permMenuList.stream().map(MenuModel::getMenuNo).collect(Collectors.toSet());
        //info(permMenuIds.toString());
        permMenuList.forEach(pmm -> {
            //按钮直接添加
            if (!MenuType.BUTTON.getCode().equals(pmm.getType())) {
                List<Menu> menuList = menuMapper.selectByParentId(pmm.getParentId());
                List<Long> menuIds = menuList.stream().map(Menu::getMenuNo).distinct().collect(Collectors.toList());
                boolean flag = permMenuIds.containsAll(menuIds);
                //log.info(pmm.getMenuNo() + " " + pmm.getParentId() + " " + menuIds.toString() + " " + flag);
                if (!flag) {
                    permMenuIds.remove(pmm.getParentId());
                }
            }
        });
        return permMenuIds;
    }

    /**
     * 获取菜单树-带权限
     *
     * @param roleIds
     * @return
     */
    public List<MenuModel> getMenuTree(List<Long> roleIds) {
        List<MenuModel> modelList = menuMapper.selectMenuPermTree(
                new MenuTreeSearch().setRoleIds(roleIds).setMenuTypes(Lists.newArrayList(MenuType.DIRECTORY.getCode(), MenuType.MENU.getCode()))
        );
        List<MenuTreeModel> menuTreeModelList = modelList.stream().map(MenuTreeModel::new).collect(Collectors.toList());
        List menuTree = TreeModelUtil.fastConvertByRootId(menuTreeModelList, 0L);
        Collections.sort(menuTree, Comparator.comparing(MenuTreeModel::getOrder));
        return menuTree;
    }

    /**
     * 获取菜单树by父级id
     *
     * @param parentId
     * @param roleIds
     * @return
     */
    public List<MenuModel> getMenuTreeByParentId(Long parentId, List<Long> roleIds) {
        List<MenuModel> modelList = menuMapper.selectMenuTreeByParentId(parentId, roleIds);
        List<MenuTreeModel> menuTreeModelList = modelList.stream().map(MenuTreeModel::new).collect(Collectors.toList());
        List menuTree = TreeModelUtil.fastConvertByRootId(menuTreeModelList, parentId);
        Collections.sort(menuTree, Comparator.comparing(MenuTreeModel::getOrder));
        return menuTree;
    }

    /**
     * 获取菜单列表
     *
     * @param roleIds
     * @return
     */
    public List<Menu> getMenuList(List<Long> roleIds) {
        return menuMapper.selectList(roleIds);
    }

    /**
     * 根据父菜单号生成菜单号
     * 根菜单的父菜单号为null
     *
     * @return
     */
    public Long generateMenuNo(Long parentMenuNo) {
        String releaseId = String.valueOf(System.currentTimeMillis());
        boolean setnx = redisUtils.lock(MENU_NO_LOCK, releaseId, FIVE_SECONDS);
        if (!setnx) {
            throw new UserCenterException(600, "系统繁忙，请稍后重试");
        }
        String menuNo;
        try {
            if (parentMenuNo == null || parentMenuNo == 0L) {
                //起始编号
                int startNo = 100;
                List<Menu> menus = menuMapper.selectByParentId(0L);
                long maxMenuNo = menus.stream().map(Menu::getMenuNo).max(Long::compareTo).orElse(0L);
                maxMenuNo = maxMenuNo + startNo;

                if (maxMenuNo <= startNo) {
                    menuNo = String.valueOf(maxMenuNo + 1);
                } else {
                    menuNo = String.valueOf(maxMenuNo);
                }
            } else {
                List<Menu> menus = menuMapper.selectByParentId(parentMenuNo);
                if (CollectionUtils.isEmpty(menus)) {
                    menuNo = parentMenuNo + "01";
                } else {
                    Long maxMenuNo = menus.stream().map(Menu::getMenuNo).max(Long::compareTo).orElse(0L);
                    long menuNoLong = maxMenuNo + 1;
                    menuNo = String.valueOf(menuNoLong);
                }
            }
        } finally {
            redisUtils.unlock(MENU_NO_LOCK, releaseId);
        }
        return Long.parseLong(menuNo);
    }

    public MenuDTO getMenuById(Long id) {
        Menu menu = menuMapper.selectById(id);
        MenuDTO menuDTO = MenuConverter.INSTANCE.do2dto(menu);
        if (menuDTO.getParentId() != 0L) {
            Menu pMenu = menuMapper.selectByMenuNo(menuDTO.getParentId());
            if (pMenu != null) {
                menuDTO.setPpId(pMenu.getId());
                menuDTO.setParentName(pMenu.getMenuName());
            }
        }
        return menuDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(MenuBO menuBO) {
        if (menuBO.getParentId() == null) {
            menuBO.setParentId(0L);
        }
        checkAddMenu(menuBO);

        Menu menu = new Menu(false);
        BeanUtils.copyProperties(menuBO, menu);

        Integer menuDept = getMenuDepth(menu, 0);
        if (menuDept >= 3) {
            throw new UserCenterException("目前系统菜单最大层级限制3级");
        }

        Long menuNo = generateMenuNo(menu.getParentId());
        menu.setMenuNo(menuNo);
        menuMapper.insertUseGeneratedKeys(menu);

        MenuPermission permission = new MenuPermission();
        permission.setPermissionTag(menu.getPermissionTag());
        permission.setInterfaceUrl(menu.getPermissionTag());
        menuPermissionMapper.insert(permission);

        //如果是超级管理员，创建菜单自动分配权限到超级管理员角色
        if (ShieldContextHolder.isSuperAdmin()) {
            RoleMenu roleMenu = new RoleMenu(true);
            roleMenu.setMenuNo(menuNo);
            roleMenu.setRoleId(ShieldContextHolder.getRoleId());
            roleMenuMapper.insert(roleMenu);
        }
    }

    public void checkAddMenu(MenuBO menuBO) {
        Menu menu = new Menu().setMenuName(menuBO.getMenuName()).setParentId(menuBO.getParentId());
        int cnt = menuMapper.selectCount(menu);
        if (cnt > 0) {
            throw new UserCenterException("同级菜单名称已存在");
        }
    }

    public Integer getMenuDepth(Menu menu, Integer depth) {
        if (menu == null || menu.getParentId() == null) {
            return 0;
        }
        depth++;
        menu = menuMapper.selectByMenuNo(menu.getParentId());
        if (menu == null || menu.getParentId() == 0) {
            return depth;
        }
        return getMenuDepth(menu, depth);
    }

    @Transactional(rollbackFor = Exception.class)
    public void edit(MenuBO menuBO) {
        if (menuBO.getParentId() == null) {
            menuBO.setParentId(0L);
        }
        checkEditMenu(menuBO);

        Menu menu = new Menu(false);
        BeanUtils.copyProperties(menuBO, menu);
        menuMapper.updateByPrimaryKeySelective(menu);
    }

    public void checkEditMenu(MenuBO menuBO) {
        Menu condition = new Menu().setMenuName(menuBO.getMenuName()).setParentId(menuBO.getParentId());
        Menu menu = menuMapper.selectOne(condition);
        if (menu != null && !menu.getId().equals(menuBO.getId())) {
            throw new UserCenterException("同级菜单名称已存在");
        }
    }

    public List<MenuPermission> getMenuPermissions(List<Long> menuIdList) {
        return menuPermissionMapper.selectByMenuIds(menuIdList);
    }

    public void logicDeleteById(Long id) {
        Menu menu = menuMapper.selectById(id);
        log.warn("{}删除菜单{}", ShieldContextHolder.getAccount(), menu.toString());
//        menuMapper.logicDeleteById(id);
        menuMapper.deleteByPrimaryKey(id);
        //删除菜单和角色关系
        roleMenuMapper.deleteByMenuNo(menu.getMenuNo());
    }

}
