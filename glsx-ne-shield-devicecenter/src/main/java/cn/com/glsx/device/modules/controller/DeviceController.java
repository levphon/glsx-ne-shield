package cn.com.glsx.device.modules.controller;

import cn.com.glsx.device.modules.service.DeviceService;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/test")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/search")
    public R search() {
        String username = deviceService.test();
        return R.ok().data(username + "-->this is a test");
    }

}
