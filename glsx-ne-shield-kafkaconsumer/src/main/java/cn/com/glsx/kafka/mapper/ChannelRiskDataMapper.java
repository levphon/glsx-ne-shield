package cn.com.glsx.kafka.mapper;

import cn.com.glsx.kafka.entity.ChannelRiskData;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChannelRiskDataMapper extends CommonBaseMapper<ChannelRiskData> {
    //工单+设备号+车架号唯一
    @Select("SELECT * FROM t_channel_risk_data WHERE workorder_no=#{workorderNo} AND vin=#{vin} AND sn=#{sn}")
    @ResultMap("BaseResultMap")
    List<ChannelRiskData> selectByWorkStandSN(@Param(value = "workorderNo") String workorderNo, @Param(value = "vin")String vin, @Param(value = "sn")String sn);
}