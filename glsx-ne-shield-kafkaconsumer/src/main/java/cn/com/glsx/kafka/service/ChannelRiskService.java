package cn.com.glsx.kafka.service;

import cn.com.glsx.kafka.converter.ChannelRiskConverter;
import cn.com.glsx.kafka.entity.ChannelRiskData;
import cn.com.glsx.kafka.mapper.ChannelRiskDataMapper;
import cn.com.glsx.kafka.model.ChannerRiskBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ChannelRiskService {
    @Autowired
    ChannelRiskDataMapper channelRiskDataMapper;

    public void process(ChannerRiskBo channerRiskBo) {
        //订单号
        String workorderNo=channerRiskBo.getWorkorderNo();
        //车架号
        String vin=channerRiskBo.getVin();
        //设备号
        String sn=channerRiskBo.getSn();

        //判断(工单+设备号+车架号唯一)
        List<ChannelRiskData> existData =channelRiskDataMapper.selectByWorkStandSN(workorderNo,vin,sn);

        //bo2do
        ChannelRiskData newData = ChannelRiskConverter.INSTANCE.toChannelRisk(channerRiskBo);

        if(existData==null || existData.size()<1){
            //如果不存在，新增数据
            channelRiskDataMapper.insert(newData);
        }else {
            //如果存在，更新数据
            newData.setId(existData.get(0).getId());
            channelRiskDataMapper.updateByPrimaryKey(newData);
        }

    }

    //TODO 测试查看数据，记得移除
    public ChannelRiskData getbean(String workorderNo,String vin,String sn) {
        List<ChannelRiskData> existData =channelRiskDataMapper.selectByWorkStandSN(workorderNo,vin,sn);
        return existData.get(0);
    }
}
