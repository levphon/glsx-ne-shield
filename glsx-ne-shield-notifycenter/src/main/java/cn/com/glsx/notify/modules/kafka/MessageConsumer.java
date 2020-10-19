package cn.com.glsx.notify.modules.kafka;

import cn.com.glsx.notify.modules.controller.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static cn.com.glsx.notify.modules.controller.WebSocketServer.SocketSet;

/**
 * @author payu
 */
@Slf4j
@Component
public class MessageConsumer {

    @KafkaListener(id = "messageConsumer",
            topics = "#{'${topic.message.application}'.split(',')}",
            errorHandler = "consumerAwareErrorHandler")
    public void listener(ConsumerRecord<?, ?> record) {
        //errorHandler异常处理器注解里面使用的也是bean的名称

        //判断是否为null
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {
            //得到Optional实例中的值
            Object message = kafkaMessage.get();
            log.info("消费消息:" + message);
            try {
                for (WebSocketServer socketServer : SocketSet) {
                    socketServer.sendMessage(message.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
