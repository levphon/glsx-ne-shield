package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.entity.User;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.model.view.UserDTO;
import org.mapstruct.Mapper;
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

    UserDTO do2dto(User user);

    User dto2do(UserDTO userDTO);

    UserBO do2bo(User user);

    User bo2do(UserBO userBO);

}
