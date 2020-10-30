package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DepartmentConverter {

    DepartmentConverter INSTANCE = Mappers.getMapper(DepartmentConverter.class);

    DepartmentDTO do2dto(Department department);

}
