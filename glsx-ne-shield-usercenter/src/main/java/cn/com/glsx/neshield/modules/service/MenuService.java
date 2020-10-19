package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.converter.MenuConverter;
import cn.com.glsx.neshield.modules.entity.Menu;
import cn.com.glsx.neshield.modules.entity.RoleMenu;
import cn.com.glsx.neshield.modules.mapper.MenuMapper;
import cn.com.glsx.neshield.modules.mapper.RoleMenuMapper;
import cn.com.glsx.neshield.modules.model.MenuBO;
import cn.com.glsx.neshield.modules.model.MenuDTO;
import cn.com.glsx.neshield.modules.model.MenuModel;
import cn.com.glsx.neshield.modules.model.MenuTreeModel;
import cn.com.glsx.neshield.modules.model.export.MenuExport;
import cn.com.glsx.neshield.modules.model.param.MenuSearch;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.glsx.plat.redis.utils.RedisUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.RedisConstants.FIVE_SECONDS;
import static cn.com.glsx.admin.common.constant.RedisConstants.MENU_NO_LOCK;

/**
 * @author: taoyr
 **/
@Service
public class MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Autowired
    private RedisUtils redisUtils;

    public PageInfo<MenuDTO> search(MenuSearch search) {
        List<MenuDTO> list = menuMapper.selectList(search);
        return new PageInfo<>(list);
    }

    public List<MenuExport> export(UserSearch search) {
        return Lists.newArrayList();
    }

    public List<MenuDTO> children(Long parentId) {
        List<Menu> menuList = menuMapper.selectMenuListByParentId(parentId);
        List<MenuDTO> list = new ArrayList<>(menuList.size());
        for (Menu menu : menuList) {
            list.add(MenuConverter.INSTANCE.do2dto(menu));
        }
        return list;
    }

    /**
     * 获取菜单树-全部菜单
     *
     * @param roleIds
     * @return
     */
    public List<MenuModel> getMenuFullTreeWithChecked(List<Long> roleIds) {
        List<MenuModel> modelList = menuMapper.selectMenuFullTree();
        List<MenuModel> permMenuList = menuMapper.selectMenuPermTree(roleIds);
        modelList.forEach(mm -> {
            for (MenuModel mm2 : permMenuList) {
                if (mm.getMenuNo().equals(mm2.getMenuNo())) {
                    mm.setChecked(true);
                    break;
                }
            }
        });
        List<MenuTreeModel> menuTreeModelList = modelList.stream().map(MenuTreeModel::new).collect(Collectors.toList());
        List menuTree = TreeModelUtil.fastConvertByRootId(menuTreeModelList, 0L);
        Collections.sort(menuTree, Comparator.comparing(MenuTreeModel::getOrder));
        return menuTree;
    }

    /**
     * 获取菜单树-带权限
     *
     * @param roleIds
     * @return
     */
    public List<MenuModel> getMenuTree(List<Long> roleIds) {
        List<MenuModel> modelList = menuMapper.selectMenuPermTree(roleIds);
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
        return menuMapper.selectMenuList(roleIds);
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
                List<Menu> menus = menuMapper.selectMenuListByParentId(0L);
                long maxMenuNo = menus.stream().map(Menu::getMenuNo).mapToLong(Long::parseLong).max().orElse(0L);
                maxMenuNo = maxMenuNo + startNo;
                if (maxMenuNo <= startNo) {
                    menuNo = String.valueOf(maxMenuNo + 1);
                } else {
                    menuNo = String.valueOf(maxMenuNo);
                }
            } else {
                List<Menu> menus = menuMapper.selectMenuListByParentId(parentMenuNo);
                if (CollectionUtils.isEmpty(menus)) {
                    menuNo = parentMenuNo + "01";
                } else {
                    String maxMenuNo = menus.stream().map(Menu::getMenuNo).max(Comparator.comparingLong(Long::parseLong)).orElse("0");
                    long menuNoLong = Long.parseLong(maxMenuNo) + 1;
                    menuNo = String.valueOf(menuNoLong);
                }
            }
        } finally {
            redisUtils.unlock(MENU_NO_LOCK, releaseId);
        }
        return Long.parseLong(menuNo);
    }

    public Menu getMenuById(Long id) {
        return menuMapper.selectByPrimaryKey(id);
    }

    public void add(MenuBO menuBO) {
        Menu menu = new Menu(false);
        BeanUtils.copyProperties(menuBO, menu);

        Long menuNo = generateMenuNo(menu.getParentId());
        menu.setMenuNo(String.valueOf(menuNo));
        menuMapper.insertUseGeneratedKeys(menu);

        //如果是超级管理员，创建菜单自动分配权限到超级管理员角色
        if (ShieldContextHolder.isSuperAdmin()) {
            RoleMenu roleMenu = new RoleMenu(true);
            roleMenu.setMenuId(menu.getId());
            roleMenu.setRoleId(ShieldContextHolder.getRoleId());
            roleMenuMapper.insert(roleMenu);
        }
    }

    public void edit(MenuBO menuBO) {
//        Menu dbMenu = menuMapper.selectByPrimaryKey(menu.getId());
//        if (!dbMenu.getParentId().equals(menu.getParentId()) || !dbMenu.getType().equals(menu.getType())) {
//        }
        Menu menu = new Menu(false);
        BeanUtils.copyProperties(menuBO, menu);
        menuMapper.updateByPrimaryKeySelective(menu);
    }

    public void logicDeleteById(Long id) {
        menuMapper.logicDeleteById(id);
    }

}
