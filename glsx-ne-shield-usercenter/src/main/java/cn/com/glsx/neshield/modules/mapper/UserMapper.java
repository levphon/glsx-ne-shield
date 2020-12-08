package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.param.UserSearch;
import cn.com.glsx.neshield.modules.model.view.DepartmentCount;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends CommonBaseMapper<User> {

    User selectById(@Param("id") Long id);

    List<User> selectByDepartmentId(@Param("departmentId") Long departmentId);

    List<User> selectExcludeUserIdByDepartmentId(@Param("departmentId") Long departmentId, @Param("userId") Long userId);

    User selectByAccount(@Param("account") String username);

    int selectCntByAccount(@Param("account") String username);

    List<User> selectList(UserSearch search);

    Integer countByCriterial(UserBO userBO);

    List<DepartmentCount> countDepartmentsUser(@Param("departmentIds") List<Long> departmentIds);

    List<User> selectDepartmentsSubordinate(UserSearch search);

    int logicDeleteById(@Param("id") Long id);

}