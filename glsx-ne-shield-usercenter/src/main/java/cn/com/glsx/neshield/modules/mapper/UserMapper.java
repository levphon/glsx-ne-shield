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

    User selectByAccount(@Param("account") String username);

    int selectCntByAccount(@Param("account") String username);

    List<User> selectList(UserSearch search);

    int logicDeleteById(@Param("id") Long id);

    int countByCriterial(UserBO userBO);

    List<DepartmentCount> countDepartmentsUser(@Param("departmentIdList") List<Long> departmentIdList);

    List<User> selectDepartmentsSubordinate(@Param("departmentList") List<Long> departmentList, @Param("userId") Long userId, @Param("search") UserSearch search);
}