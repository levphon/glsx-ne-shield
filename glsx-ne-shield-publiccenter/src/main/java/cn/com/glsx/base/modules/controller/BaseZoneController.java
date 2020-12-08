package cn.com.glsx.base.modules.controller;

import cn.com.glsx.base.modules.entity.BaseZone;
import cn.com.glsx.base.modules.service.BaseZoneService;
import com.glsx.plat.core.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * @author liuyf
 * @date 2019-04-03 11:33:49
 */
@RestController
@RequestMapping("basic/zone")
public class BaseZoneController {

    @Autowired
    private BaseZoneService baseZoneService;

    @RequestMapping("/list")
    public R list(@NotBlank(message = "城市编码不能为空") String cityCode) {
        List<BaseZone> list = baseZoneService.searchByCityCode(cityCode);
        return R.ok().data(list);
    }

}
