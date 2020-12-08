package cn.com.glsx.kafka.converter;

import cn.com.glsx.kafka.entity.ChannelRiskData;
import cn.com.glsx.kafka.model.ChannerRiskBo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChannelRiskConverter {

    ChannelRiskConverter INSTANCE = Mappers.getMapper(ChannelRiskConverter.class);

    ChannelRiskData toChannelRisk(ChannerRiskBo menu);

}
