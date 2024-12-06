package com.fit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@ServletComponentScan
@SpringBootApplication
@EntityScan(basePackages = {"com.fit.entity"})
public class TreeNmsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(TreeNmsApplication.class, args);
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = run.getEnvironment().getProperty("server.port");

        log.info("\n---------------------------------------------------------\n" +
                "Application TreeNMS is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + "/\n\t" +
                "External:\thttps://" + ip + ":" + port + "/" +
                "\n-----------------页面请部署 admin-web----------------------");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(TreeNmsApplication.class);
    }
}
