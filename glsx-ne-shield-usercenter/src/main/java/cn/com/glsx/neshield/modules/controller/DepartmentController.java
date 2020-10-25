package cn.com.glsx.neshield.modules.controller;

import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: taoyr
 **/
@Slf4j
@RestController
@RequestMapping(value = "/department")
public class DepartmentController {

    private final static String MODULE = "部门管理";

    @GetMapping("/simplelist")
    public R simpleList() {

        return R.ok();
    }

}
