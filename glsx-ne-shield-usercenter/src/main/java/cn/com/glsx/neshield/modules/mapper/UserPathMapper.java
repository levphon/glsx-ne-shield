package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.UserPath;
import cn.com.glsx.neshield.modules.model.view.DepartmentUserCount;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserPathMapper extends CommonBaseMapper<UserPath> {

    List<DepartmentUserCount> selectSubordinateDepartmentList(Long userId);

    List<UserPath> selectUserSubordinateList(Long userId);
}