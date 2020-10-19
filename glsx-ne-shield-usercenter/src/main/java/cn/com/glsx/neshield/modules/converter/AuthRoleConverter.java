package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthRoleConverter {

    AuthRoleConverter INSTANCE = Mappers.getMapper(AuthRoleConverter.class);

    @Mappings(@Mapping(source = "id", target = "roleId"))
    cn.com.glsx.auth.model.Role toAuthRole(Role role);

}
