package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.UserRoleRelation;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleRelationMapper extends CommonBaseMapper<UserRoleRelation> {

    /**
     * 获取用户角色列表
     *
     * @param relation
     * @return
     */
    List<UserRoleRelation> selectUserRoleRelationList(UserRoleRelation relation);

}