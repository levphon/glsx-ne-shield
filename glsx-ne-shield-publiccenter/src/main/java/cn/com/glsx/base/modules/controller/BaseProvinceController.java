package cn.com.glsx.base.modules.controller;

import cn.com.glsx.base.modules.entity.BaseProvince;
import cn.com.glsx.base.modules.service.BaseProvinceService;
import com.glsx.plat.core.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 省份
 *
 * @author Mark
 * @date 2019-03-28 10:17:01
 */
@RestController
@RequestMapping("basic/province")
public class BaseProvinceController {

    @Autowired
    private BaseProvinceService baseProvinceService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list() {
        List<BaseProvince> list = baseProvinceService.list();
        return R.ok().data(list);
    }

}
