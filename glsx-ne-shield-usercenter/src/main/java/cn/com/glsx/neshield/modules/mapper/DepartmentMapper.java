package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Department;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper extends CommonBaseMapper<Department> {

    int logicDeleteByIdList(List<Long> idList);

    List<Department> selectDepartmentList(Department department);

    List<Department> selectByIds(List<Long> ids);

    List<Department> selectAllNotDel();
}