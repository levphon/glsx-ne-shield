package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.model.param.DepartmentSearch;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends CommonBaseMapper<Department> {

    List<Department> search(DepartmentSearch search);

    List<Department> selectByIds(@Param("ids") List<Long> ids);

    List<Department> selectAllNotDel();

    Department selectById(@Param("id") Long departmentId);

    int logicDeleteByIdList(@Param("ids") List<Long> ids);

    /**
     * 根据根组织标识和租户id获取根组织
     *
     * @param tenantId
     * @return
     */
    Department selectRootDepartmentByTenantId(@Param("tenantId") Long tenantId);

}