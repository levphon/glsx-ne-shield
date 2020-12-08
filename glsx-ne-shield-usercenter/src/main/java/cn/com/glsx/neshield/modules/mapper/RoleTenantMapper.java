package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.RoleTenant;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleTenantMapper extends CommonBaseMapper<RoleTenant> {

    List<RoleTenant> selectByRoleId(Long roleId);

    List<Long> selectTenantIdsByRoleId(Long roleId);

    List<Long> selectRoleIdsByTenantId(Long roleId);

    List<RoleTenant> selectByTenantId(Long tenantId);

    int logicDeleteByRoleId(Long roleId);

}