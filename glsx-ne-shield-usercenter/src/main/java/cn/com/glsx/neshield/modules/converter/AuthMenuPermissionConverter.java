package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.MenuPermission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthMenuPermissionConverter {

    AuthMenuPermissionConverter INSTANCE = Mappers.getMapper(AuthMenuPermissionConverter.class);

    cn.com.glsx.auth.model.MenuPermission toAuthMenuPermission(MenuPermission permission);

}
