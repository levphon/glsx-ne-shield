package cn.com.glsx.kafka.config;

import cn.com.glsx.kafka.queue.consumer.ChannelRiskConsumer;
import kafka.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConsumerConfig {
    @Autowired
    private ConsumerConfig consumerConfig;

    @Value("${consumer.nThreads}")
    private Integer nThreads;

    @Bean(initMethod = "init",destroyMethod = "destroy")
    ChannelRiskConsumer channelRiskConsumer(@Value("${mq.kafka.channelRisk.topic}") String topic){
        ChannelRiskConsumer channelRiskConsumer = new ChannelRiskConsumer();
        channelRiskConsumer.setnThreads(nThreads);
        channelRiskConsumer.setConsumerConfig(consumerConfig);
        channelRiskConsumer.setTopic(topic);
        return channelRiskConsumer;
    }
}
