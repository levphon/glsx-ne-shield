package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Tenant;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends CommonBaseMapper<Tenant> {

    int logicDeleteById(Long id);

}