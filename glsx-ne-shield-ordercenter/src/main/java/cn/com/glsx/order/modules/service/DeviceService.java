package cn.com.glsx.order.modules.service;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.order.modules.entity.Device;
import cn.com.glsx.order.modules.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    public String test() {
        String username = ShieldContextHolder.getAccount();
        return username;
    }

    public List<Device> selectList() {
        return deviceMapper.selectList();
    }

}
