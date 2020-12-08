package cn.com.glsx;

import com.glsx.plat.context.EnableRestAdmin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import javax.annotation.PostConstruct;
import java.util.TimeZone;
import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;

/**
 * @author fengzhi
 */
@SpringBootApplication(scanBasePackages = {"cn.com.glsx"})
@EnableRestAdmin
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(getTimeZone(of("Asia/Shanghai")));
    }

}
