package cn.com.glsx.kafka.config;

import cn.com.glsx.kafka.constant.KafkaConstant;
import kafka.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

/**
 * @author fengzhi
 * @time 2020/12/4
 * @function kafka的公共配置
 */
@Configuration
@EnableConfigurationProperties(KafkaNacosProperties.class)
public class KafkaConfig {
    @Autowired
    KafkaNacosProperties kafkaProperties;

    @Bean
    public PropertiesFactoryBean consumerProperties() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setProperties(getProperties());
        return propertiesFactoryBean;
    }

    @Bean
    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig(getProperties());
    }

    //nacos属性封装Properties
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(KafkaConstant.ZOOKEEPER_CONNECT, kafkaProperties.getConnect());
        properties.setProperty(KafkaConstant.ZOOKEEPER_CONNECT_TIMEOUT, kafkaProperties.getConnectTimeout());
        properties.setProperty(KafkaConstant.ZOOKEEPER_SESSION_TIMEOUT, kafkaProperties.getSessionTimeout());
        properties.setProperty(KafkaConstant.GROUP_ID, kafkaProperties.getGroupID());
        properties.setProperty(KafkaConstant.AUTO_COMMIT_INTERVAL, kafkaProperties.getAutoCommitInterval());
        properties.setProperty(KafkaConstant.AUTO_OFFSET_RESET, kafkaProperties.getAutoOffsetReset());
        properties.setProperty(KafkaConstant.REBLANCE_BACKOFF, kafkaProperties.getRebalanceBackoff());
        properties.setProperty(KafkaConstant.REBLANCE_MAX_RETRIES, kafkaProperties.getRebalanceMaxRetries());
        return properties;
    }
}
