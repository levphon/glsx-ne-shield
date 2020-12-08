package cn.com.glsx.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengzhi
 * @time 2020/12/4
 * @function kafka的nacos配置属性
 */
@Data
@ConfigurationProperties(prefix = "kafka.config")
public class KafkaNacosProperties {
    //nacos获取属性
    private String connect;

    private String connectTimeout;

    private String sessionTimeout;

    private String groupID;

    private String autoCommitInterval;

    private String autoOffsetReset;

    private String rebalanceBackoff;

    private String rebalanceMaxRetries;
}
