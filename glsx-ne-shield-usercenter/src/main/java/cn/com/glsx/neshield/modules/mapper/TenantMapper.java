package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Tenant;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TenantMapper extends CommonBaseMapper<Tenant> {

    Tenant selectById(@Param("id") Long tenantId);

    int logicDeleteById(Long id);

}