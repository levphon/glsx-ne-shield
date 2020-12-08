package cn.com.glsx.base.modules.controller;

import cn.com.glsx.base.modules.entity.BaseCity;
import cn.com.glsx.base.modules.service.BaseCityService;
import com.glsx.plat.core.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 市区
 *
 * @author Mark
 * @date 2019-03-28 13:49:24
 */
@RestController
@RequestMapping("basic/city")
@Validated
public class BaseCityController {

    @Autowired
    private BaseCityService baseCityService;

    /**
     * 根据条件查询
     */
    @RequestMapping("/list")
    public R list(@NotBlank(message = "省份编码不能为空") String provCode) {
        List<BaseCity> list = baseCityService.list(provCode);
        return R.ok().data(list);
    }

}
