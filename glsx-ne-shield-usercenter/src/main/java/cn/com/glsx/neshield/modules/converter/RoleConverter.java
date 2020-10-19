package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.model.RoleDTO;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    RoleDTO do2dto(Role role);

    @Mappings(
            {
                    @Mapping(source = "roleName", target = "roleName"),
                    @Mapping(source = "remark", target = "remark"),
                    @Mapping(source = "permissionType", target = "rolePermissionType"),
                    @Mapping(source = "visibleType", target = "roleVisibility"),
                    @Mapping(source = "visibleTenant", target = "roleTenants"),
                    @Mapping(source = "enableStatus", target = "enableStatus"),
                    @Mapping(source = "id", target = "id"),
            }
    )
    Role boToDo(RoleBO roleBO);



}
