package cn.com.glsx.neshield.modules.service;

import cn.com.glsx.neshield.modules.entity.Device;
import cn.com.glsx.neshield.modules.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: taoyr
 **/
@Service
public class DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    public List<Device> getAll() {
        return deviceMapper.selectAll();
    }

}
