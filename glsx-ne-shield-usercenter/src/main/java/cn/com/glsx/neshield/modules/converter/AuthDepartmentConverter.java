package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthDepartmentConverter {

    AuthDepartmentConverter INSTANCE = Mappers.getMapper(AuthDepartmentConverter.class);

    @Mappings(@Mapping(source = "id", target = "deptId"))
    cn.com.glsx.auth.model.Department toAuthDepartment(Department department);

}
