package cn.com.glsx.order.modules.controller;

import cn.com.glsx.auth.model.FunctionPermissions;
import cn.com.glsx.order.modules.entity.Device;
import cn.com.glsx.order.modules.service.DeviceService;
import com.glsx.plat.core.web.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.com.glsx.auth.model.FunctionPermissionType.DEVICE_QUERY;

@Slf4j
@RestController
@RequestMapping(value = "/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @FunctionPermissions(permissionType = DEVICE_QUERY)
    @GetMapping("/search")
    public R search() {
        String username = deviceService.test();
        return R.ok().data(username + "-->this is a test");
    }

    @GetMapping("/all")
    public R all() {
        List<Device> list = deviceService.selectList();
        return R.ok().data(list);
    }

}
