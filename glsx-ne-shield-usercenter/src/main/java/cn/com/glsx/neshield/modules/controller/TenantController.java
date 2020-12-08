package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.model.param.OrganizationSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import cn.com.glsx.neshield.modules.service.DepartmentService;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/tenant")
public class TenantController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/simplelist")
    public R simpleList() {
        OrganizationSearch search = new OrganizationSearch()
                .setForPage(false)
                .setHasChild(false)
                .setHasUserNumber(false);
        PageInfo<DepartmentDTO> pageInfo = departmentService.rootDepartmentList(search);
        return R.ok().data(pageInfo.getList());
    }

}
