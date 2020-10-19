package cn.com.glsx.base.modules.converter;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DictDataConverter {

    DictDataConverter INSTANCE = Mappers.getMapper(DictDataConverter.class);

    cn.com.glsx.base.modules.model.DictDataDTO do2dto(cn.com.glsx.base.modules.entity.SysDictData data);

    cn.com.glsx.base.modules.entity.SysDictData bo2do(cn.com.glsx.base.modules.model.DictDataBO dataBO);

}
