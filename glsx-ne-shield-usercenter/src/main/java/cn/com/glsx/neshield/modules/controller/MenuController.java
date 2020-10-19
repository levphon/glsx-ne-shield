package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.BaseController;
import cn.com.glsx.neshield.modules.converter.MenuConverter;
import cn.com.glsx.neshield.modules.entity.Menu;
import cn.com.glsx.neshield.modules.model.MenuBO;
import cn.com.glsx.neshield.modules.model.MenuDTO;
import cn.com.glsx.neshield.modules.model.MenuModel;
import cn.com.glsx.neshield.modules.model.export.MenuExport;
import cn.com.glsx.neshield.modules.model.param.MenuSearch;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.service.MenuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.office.excel.EasyExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/menu")
public class MenuController extends BaseController {

    private final static String MODULE = "菜单管理";

    @Resource
    private MenuService menuService;

    @GetMapping("/search")
    public R search(MenuSearch search) {
        //查子菜单数据不分页
        if (Objects.isNull(search.getParentId())) {
            PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        } else {
            PageHelper.startPage(1, Short.MAX_VALUE);
        }
        PageInfo<MenuDTO> pageInfo = menuService.search(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping("/children")
    public R children(@RequestParam("parentId") Long parentId) {
        List<MenuDTO> list = menuService.children(parentId);
        return R.ok().data(list);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, UserSearch search) throws Exception {
        List<MenuExport> list = menuService.export(search);
        EasyExcelUtils.writeExcel(response, list, "菜单_" + DateUtils.formatSerial(new Date()), "Sheet1", MenuExport.class);
    }

    @GetMapping("/fulltree")
    public R getMenuFullTree() {
        List<MenuModel> menuTree = menuService.getMenuFullTreeWithChecked(ShieldContextHolder.getRoleIds());
        return R.ok().data(menuTree);
    }

    @GetMapping("/permtree")
    public R getMenuPermTree() {
        List<MenuModel> menuTree = menuService.getMenuTree(ShieldContextHolder.getRoleIds());
        return R.ok().data(menuTree);
    }

    @GetMapping("/subtree")
    public R getMenuSubtree(@RequestParam("parentId") Long parentId) {
        List<MenuModel> menuTree = menuService.getMenuTreeByParentId(parentId, ShieldContextHolder.getRoleIds());
        return R.ok().data(menuTree);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping("/add")
    public R add(@RequestBody @Valid MenuBO menuBO) {
        menuService.add(menuBO);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping("/edit")
    public R edit(@RequestBody @Valid MenuBO menuBO) {
        AssertUtils.isNull(menuBO.getId(), "ID不能为空");
        menuService.edit(menuBO);
        return R.ok();
    }

    @GetMapping("/info")
    public R info(@RequestParam("menuId") Long id) {
        Menu menu = menuService.getMenuById(id);
        MenuDTO menuDTO = MenuConverter.INSTANCE.do2dto(menu);
        return R.ok().data(menuDTO);
    }

    @SysLog
    @GetMapping("/delete")
    public R delete(@RequestParam("menuId") Long id) {
        menuService.logicDeleteById(id);
        return R.ok();
    }

}
