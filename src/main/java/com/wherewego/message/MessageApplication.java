package com.wherewego.message;

import com.wherewego.message.service.NettyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms    //启动消息队列
public class MessageApplication {

    public static void main(String[] args) {
        //禁用web组件
        SpringApplication springApplication =
                new SpringApplicationBuilder()
                        .sources(MessageApplication.class)
                        .web(WebApplicationType.NONE)
                        .build();

        springApplication.run(args);
//        SpringApplication.run(MessageApplication.class, args);
    }
}
