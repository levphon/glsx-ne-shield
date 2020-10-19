package cn.com.glsx.device.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    public String test() {
        String username = ShieldContextHolder.getUsername();
        return username;
    }

}
