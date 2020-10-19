package cn.com.glsx.notify.modules.service;

import cn.com.glsx.admin.services.notifyservice.model.PCTemplateMessageDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;

/**
 * @author payu
 */
@Slf4j
@Service
public class PCService {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(PCTemplateMessageDTO messageDTO) {
        // 发送消息，json 作为消息体
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(messageDTO.getApplication() + "_topic", JSONObject.toJSONString(messageDTO));

        // 监听回调
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("## Send message success ...");
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("## Send message fail ...{}", throwable.getMessage());
            }

        });
    }

}
