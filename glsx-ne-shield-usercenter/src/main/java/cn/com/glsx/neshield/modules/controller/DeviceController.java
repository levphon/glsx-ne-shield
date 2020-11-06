package cn.com.glsx.neshield.modules.controller;

import cn.com.glsx.neshield.modules.entity.Device;
import cn.com.glsx.neshield.modules.service.DeviceService;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/all")
    public R all() {
        List<Device> list = deviceService.getAll();
        return R.ok().data(list);
    }

}
