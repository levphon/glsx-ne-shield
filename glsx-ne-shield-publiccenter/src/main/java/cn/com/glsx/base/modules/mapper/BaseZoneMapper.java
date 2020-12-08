package cn.com.glsx.base.modules.mapper;

import cn.com.glsx.base.modules.entity.BaseZone;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BaseZoneMapper extends CommonBaseMapper<BaseZone> {

    List<BaseZone> selectByCityCode(@Param("cityCode") String cityCode);

    BaseZone selectByCode(@Param("code") String zoneCode);

}