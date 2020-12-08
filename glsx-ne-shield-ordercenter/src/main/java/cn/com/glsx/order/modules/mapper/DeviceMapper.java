package cn.com.glsx.order.modules.mapper;

import cn.com.glsx.order.modules.entity.Device;
import com.glsx.plat.common.annotation.DataPerm;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeviceMapper extends CommonBaseMapper<Device> {

    @DataPerm
    List<Device> selectList();

}