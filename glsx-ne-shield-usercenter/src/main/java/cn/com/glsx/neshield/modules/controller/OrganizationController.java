package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.model.OrgModel;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import cn.com.glsx.neshield.modules.service.OrganizationService;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController {

    private final static String MODULE = "组织管理";

    @Resource
    private OrganizationService organizationService;

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/search")
    public R rootList(OrganizationSearch search) {
        search.setForPage(true);
        search.setHasChild(true);
        search.setHasUserNumber(false);
        List<DepartmentDTO> list = departmentService.rootDepartmentList(search);
        return R.ok().data(list);
    }

    @GetMapping("/children")
    public R children(OrganizationSearch search) {
        AssertUtils.isNull(search.getRootId(), "参数有误");
        List<DepartmentDTO> list = organizationService.childrenList(search);
        return R.ok().data(list);
    }

    @GetMapping("/orgtree")
    public R orgTree(OrgTreeSearch search) {
        List<? extends TreeModel> list = organizationService.orgTree(search);
        return R.ok().data(list);
    }

    @GetMapping("/simplelist")
    public R simpleList(Long rootId) {
        List<DepartmentDTO> simpleList = organizationService.simpleList(rootId);
        return R.ok().data(simpleList);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping("/add")
    public R addOrganization(@RequestBody @Valid OrganizationBO orgBO) {
        if (orgBO.getSuperiorId() == null) {
            organizationService.addRootOrganization(orgBO);
        } else {
            organizationService.addNodeToOrganization(orgBO);
        }
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping("/edit")
    public R editOrganization(@RequestBody @Valid OrganizationBO orgBO) {
        AssertUtils.isNull(orgBO.getOrganizationId(), "参数有误");
        organizationService.editOrganization(orgBO);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.DELETE)
    @GetMapping("/delete")
    public R deleteOrganization(@RequestParam("id") Long organizationId) {
        organizationService.deleteOrganization(organizationId);
        return R.ok();
    }

    @GetMapping("/info")
    public R organizationInfo(@RequestParam("id") Long organizationId) {
        OrgModel orgModel = organizationService.organizationInfo(organizationId);
        return R.ok().data(orgModel);
    }

    @GetMapping("/strategy")
    public R rolePermission(@RequestParam("type") Integer rolePermissionType) {
        organizationService.permissionStrategy(rolePermissionType);
        return R.ok();
    }

}
