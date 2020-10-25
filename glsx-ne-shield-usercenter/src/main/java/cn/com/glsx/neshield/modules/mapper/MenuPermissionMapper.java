package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.MenuPermission;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuPermissionMapper extends CommonBaseMapper<MenuPermission> {

    List<MenuPermission> selectByMenuIds(@Param("menuIds") List<Long> menuIds);

}