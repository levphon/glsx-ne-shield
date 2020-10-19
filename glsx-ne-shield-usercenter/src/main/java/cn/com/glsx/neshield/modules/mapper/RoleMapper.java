package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.model.param.RoleSearch;
import cn.com.glsx.neshield.modules.entity.Role;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends CommonBaseMapper<Role> {

    List<Role> selectList(RoleSearch search);

    /**
     * 根据roleIds获取角色列表
     *
     * @param roleIds
     * @return
     */
    List<Role> selectRoleListByIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据用户id查询角色列表
     *
     * @return
     */
    List<Role> selectUserRoleList(Long userId);

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    int logicDelete(Long id);
}