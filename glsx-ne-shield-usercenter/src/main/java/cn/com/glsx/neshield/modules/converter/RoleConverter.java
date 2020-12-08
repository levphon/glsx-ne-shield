package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    //RoleDTO do2dto(Role role);

    @Mappings({
            @Mapping(source = "roleId", target = "id"),
            @Mapping(source = "roleName", target = "roleName"),
            @Mapping(source = "remark", target = "remark"),
            @Mapping(source = "rolePermissionType", target = "rolePermissionType"),
            @Mapping(source = "roleVisibility", target = "roleVisibility"),
            @Mapping(source = "enableStatus", target = "enableStatus"),
    })
    Role bo2do(RoleBO roleBO);

}
