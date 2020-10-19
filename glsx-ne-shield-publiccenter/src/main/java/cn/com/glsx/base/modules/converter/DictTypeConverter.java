package cn.com.glsx.base.modules.converter;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DictTypeConverter {

    DictTypeConverter INSTANCE = Mappers.getMapper(DictTypeConverter.class);

    cn.com.glsx.base.modules.model.DictTypeDTO do2dto(cn.com.glsx.base.modules.entity.SysDictType type);

    cn.com.glsx.base.modules.entity.SysDictType bo2do(cn.com.glsx.base.modules.model.DictTypeBO typeBO);

}
