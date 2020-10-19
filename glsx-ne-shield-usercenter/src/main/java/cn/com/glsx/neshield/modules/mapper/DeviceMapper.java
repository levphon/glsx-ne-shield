package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.auth.model.RequireDataPermissions;
import cn.com.glsx.neshield.modules.entity.Device;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceMapper extends CommonBaseMapper<Device> {

    @RequireDataPermissions(linkField = "id")
    List<Device> selectDeviceList(@Param("creatorIds") List<Integer> creatorIds, @Param("receiveId") Integer receiveId);

}