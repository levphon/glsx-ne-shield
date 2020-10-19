package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.model.param.OrganizationBO;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import cn.com.glsx.neshield.modules.service.OrganizationService;
import com.glsx.plat.core.constant.ResultConstants;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private DepartmentService departmentService;

    @RequestMapping("/add")
    public R addOrganization(OrganizationBO organizationBO) {
        if (organizationBO == null) {
            return R.error(ResultConstants.ARGS_NULL);
        }
        if (organizationBO.getRootId() == null) {
            return organizationService.addRootOrganization(organizationBO);
        } else {
            return organizationService.addNodeToOrganization(organizationBO);
        }
    }

    @RequestMapping("/edit")
    public R editOrganization(OrganizationBO organizationBO) {
        if (organizationBO == null || organizationBO.getOrganizationId() == null) {
            return R.error(ResultConstants.ARGS_NULL);
        }

        return organizationService.editOrganization(organizationBO);
    }

    @RequestMapping("/delete")
    public R deleteOrganization(Long organizationId) {
        return organizationService.deleteOrganization(organizationId);
    }

    @RequestMapping("/info")
    public R organizationInfo(Long organizationId) {
        return organizationService.organizationInfo(organizationId);
    }

    @RequestMapping("/rootList")
    public R rootList(OrganizationSearch organizationSearch) {
        if (organizationSearch == null || organizationSearch.getPageSize() <= 0 || organizationSearch.getPageNumber() <= 0) {
            return R.error(ResultConstants.ARGS_ERROR);
        }
        organizationSearch.setForPage(true);
        return departmentService.rootDepartmentList(organizationSearch);
    }

    @RequestMapping("/children")
    public R children(OrganizationSearch organizationSearch) {
        if (organizationSearch == null || organizationSearch.getRootId() == null) {
            return R.error(ResultConstants.ARGS_ERROR);
        }

        return organizationService.childrenList(organizationSearch);
    }

    @RequestMapping("/simpleList")
    public R simpleList(Long rootId) {
        return organizationService.simpleList(rootId);
    }

    @RequestMapping("/treeOrg")
    public R treeOrg(String departmentName){
        return organizationService.treeOrg(departmentName);
    }

}
