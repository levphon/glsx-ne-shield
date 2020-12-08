package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.admin.common.constant.UserConstants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.common.exception.UserCenterException;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.com.glsx.neshield.modules.model.view.RoleDTO;
import cn.com.glsx.neshield.modules.model.view.SimpleRoleDTO;
import cn.com.glsx.neshield.modules.service.RoleService;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.exception.SystemMessage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.com.glsx.admin.common.constant.UserConstants.RoleVisibility.onlyAdmin;
import static cn.com.glsx.admin.common.constant.UserConstants.adminRoleId;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/role")
public class RoleController {

    private final static String MODULE = "角色管理";

    @Autowired
    private RoleService roleService;

    @GetMapping("/search")
    public R search(RoleSearch search) {
        PageInfo<RoleDTO> pageInfo = roleService.search(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping("/simplelist")
    public R simplelist() {
        List<SimpleRoleDTO> list = roleService.simpleList();
        return R.ok().data(list);
    }

    @GetMapping("/visibility")
    public R visibility() {
        List<UserConstants.RoleVisibility> vList = Arrays.asList(UserConstants.RoleVisibility.values());
        List<Map<String, Object>> list = Lists.newArrayList();
        vList.forEach(rv -> {
            Map<String, Object> map = Maps.newHashMap();
            map.put("code", rv.getCode());
            map.put("value", rv.getValue());
            if (ShieldContextHolder.isSuperAdmin()) {
                list.add(map);
            } else {
                if (!onlyAdmin.getCode().equals(rv.getCode())) {
                    list.add(map);
                }
            }
        });
        return R.ok().data(list);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping("/add")
    public R add(@RequestBody @Valid RoleBO roleBO) {
        roleService.addRole(roleBO);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping("/edit")
    public R edit(@RequestBody @Valid RoleBO roleBO) {
        AssertUtils.isNull(roleBO.getRoleId(), "ID不能为空");
        if (roleBO.getRoleId().equals(adminRoleId)) {
            throw UserCenterException.of(SystemMessage.OPERATE_PERMISSION_DENIED);
        }
        roleService.editRole(roleBO);
        return R.ok();
    }

    @GetMapping("/info")
    public R info(@RequestParam("id") Long id) {

        RoleDTO roleDTO = roleService.roleInfo(id);

        return R.ok().data(roleDTO);
    }

    //    @RequireFunctionPermissions(permissionType = FunctionPermissionType.ROLE_DELETE)
    @SysLog(module = MODULE, action = OperateType.DELETE)
    @GetMapping("/delete")
    public R delete(@RequestParam("id") Long id) {
        if (id.equals(adminRoleId)) {
            throw UserCenterException.of(SystemMessage.OPERATE_PERMISSION_DENIED);
        }
        roleService.deleteRole(id);
        return R.ok();
    }

}
