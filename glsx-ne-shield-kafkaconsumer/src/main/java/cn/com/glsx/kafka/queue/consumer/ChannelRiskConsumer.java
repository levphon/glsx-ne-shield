package cn.com.glsx.kafka.queue.consumer;

import cn.com.glsx.kafka.model.ChannerRiskBo;
import cn.com.glsx.kafka.protocol.ChannelRisk;
import cn.com.glsx.kafka.queue.base.MessageReceiver;
import cn.com.glsx.kafka.service.ChannelRiskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;

/**
 * 渠道风险数据消费
 * topic：channel_risk
 */
@Slf4j
public class ChannelRiskConsumer extends MessageReceiver {
    @Autowired
    private ChannelRiskService channelRiskService;

    @Override
    public void processMessage(String topic, byte[] message){
        try {
            ChannelRisk.ChannelRiskInfo info = ChannelRisk.ChannelRiskInfo.parseFrom(message);
            log.info("channelRisk--开始消费："+info.getWORKORDERNO());
            ChannerRiskBo channerRiskBo = convertChannerRisk(info);
            channelRiskService.process(channerRiskBo);
            log.info("channelRisk--消费完成："+channerRiskBo.toString());
        }catch (Exception e){
            log.error("channelRisk--消费异常："+e.getMessage(),e);
        }
    }

    private ChannerRiskBo convertChannerRisk(ChannelRisk.ChannelRiskInfo info) {
        ChannerRiskBo bo = new ChannerRiskBo();
        bo.setWorkorderNo(info.getWORKORDERNO());
        bo.setOwnerName(info.getOWNERNAME());
        bo.setVin(info.getVIN());
        bo.setSn(info.getSN());
        bo.setActiveDate(getDate(info.getACTIVEDATE()));
        bo.setDevType(Integer.parseInt(info.getDEVTYPE()));
        bo.setGpsTime(getDate(info.getGPSTIME()));
        bo.setLat(Double.parseDouble(info.getLAT()));
        bo.setLng(Double.parseDouble(info.getLNG()));
        bo.setProv(info.getPROV());
        bo.setCity(info.getCITY());
        bo.setZone(info.getZONE());
        bo.setAddress(info.getADDRESS());
        bo.setShopName(info.getSHOPNAME());

        String labels = info.getLABELS();
        bo.setLabels(labels);
        //如果labels=-1,type=1疑似风险,反之为0
        bo.setType(labels.equals("-1")?1:0);

        bo.setRadius(Integer.parseInt(info.getRADIUS()));
        bo.setDistrict(info.getDISTRICT());
        bo.setStreet(info.getSTREET());
        bo.setStreetNumber(info.getSTREETNUMBER());
        bo.setCenterLat(Double.parseDouble(info.getCENTERLAT()));
        bo.setCenterLng(Double.parseDouble(info.getCENTERLNG()));
        return bo;
    }

    private Date getDate(String time_long) {
        return new Date(time_long);
    }
}
