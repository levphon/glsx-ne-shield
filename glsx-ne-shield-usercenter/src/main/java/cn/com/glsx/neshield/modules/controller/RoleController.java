package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.model.param.RoleBO;
import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.com.glsx.neshield.modules.service.RoleService;
import com.glsx.plat.core.constant.ResultConstants;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.com.glsx.admin.common.constant.UserConstants.adminRoleId;
import static cn.com.glsx.admin.common.constant.UserConstants.roleVisibility.specifyTenants;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/search")
    public R search(RoleSearch search) {
        return roleService.search(search);
    }

    @GetMapping("/simplelist")
    public R simplelist() {
        return roleService.simpleList();
    }

    @PostMapping("/add")
    public R add(@RequestBody @Validated RoleBO roleBO) {
        if (specifyTenants.getCode().equals(roleBO.getVisibleType())) {
            if (CollectionUtils.isEmpty(roleBO.getMenuIdList())) {
                return R.error(ResultConstants.ARGS_NULL);
            }
        }
        return roleService.addRole(roleBO);
    }

    @PostMapping("/edit")
    public R edit(@RequestBody @Validated RoleBO roleBO) {
        if (roleBO.getRoleId() == null) {
            return R.error(ResultConstants.ARGS_NULL);
        }
        if (roleBO.getRoleId().equals(adminRoleId)){
            return R.error("超级管理员不可编辑");
        }
        if (specifyTenants.getCode().equals(roleBO.getVisibleType())) {
            if (CollectionUtils.isEmpty(roleBO.getMenuIdList())) {
                return R.error(ResultConstants.ARGS_NULL);
            }
        }
        return roleService.editRole(roleBO);
    }

    @GetMapping("/info")
    public R info(Long id) {
        if (id == null) {
            return R.error(ResultConstants.ARGS_NULL);
        }
        return roleService.roleInfo(id);
    }

    @GetMapping("/delete")
    public R delete(Long id) {
        if (id == null) {
            return R.error(ResultConstants.ARGS_NULL);
        }
        if (id.equals(adminRoleId)){
            return R.error("超级管理员不可编辑");
        }
        return roleService.deleteRole(id);
    }

}
