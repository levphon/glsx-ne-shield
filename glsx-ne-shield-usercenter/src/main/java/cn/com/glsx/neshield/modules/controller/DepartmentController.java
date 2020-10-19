package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.service.DepartmentService;
import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/simpleRootList")
    public R simpleRootList(){
        OrganizationSearch organizationSearch = new OrganizationSearch();
        organizationSearch.setForPage(false);
        return departmentService.rootDepartmentList(organizationSearch);
    }

}
