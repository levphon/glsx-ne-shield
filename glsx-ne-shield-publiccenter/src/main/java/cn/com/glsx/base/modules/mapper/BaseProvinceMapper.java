package cn.com.glsx.base.modules.mapper;

import cn.com.glsx.base.modules.entity.BaseProvince;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BaseProvinceMapper extends CommonBaseMapper<BaseProvince> {

    BaseProvince selectByCode(@Param("provCode") String provCode);

}