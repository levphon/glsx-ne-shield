package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.UserDTO;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 解耦dao与api的实体映射
 * 字段名不一样，用@Mapping来处理
 * https://github.com/mapstruct/mapstruct-examples
 *
 * @author payu
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mappings(@Mapping(source = "username", target = "username"))
    UserDTO do2dto(User user);

    @Mappings(@Mapping(source = "username", target = "username"))
    User dto2do(UserDTO userDTO);

    UserBO do2bo(User user);

    @Mappings(
            {
                    @Mapping(source = "username", target = "username"),
                    @Mapping(source = "id", target = "id"),
            }
    )
    User bo2do(UserBO userBO);

}
