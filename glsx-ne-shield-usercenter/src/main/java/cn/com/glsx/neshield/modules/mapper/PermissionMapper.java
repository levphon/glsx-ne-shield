package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Permission;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends CommonBaseMapper<Permission> {
}