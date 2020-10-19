package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.RoleMenu;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMenuMapper extends CommonBaseMapper<RoleMenu> {

    /**
     * 逻辑删除
     *
     * @param roleId
     * @return
     */
    int logicDelByRoleId(Long roleId);
}