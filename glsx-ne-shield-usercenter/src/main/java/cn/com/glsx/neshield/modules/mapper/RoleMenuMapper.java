package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.RoleMenu;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends CommonBaseMapper<RoleMenu> {

    List<RoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    List<RoleMenu> selectByMenuId(@Param("menuId") Long menuId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 逻辑删除
     *
     * @param roleId
     * @return
     */
    int logicDelByRoleId(Long roleId);

}