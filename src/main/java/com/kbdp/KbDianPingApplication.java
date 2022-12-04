package com.kbdp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.kbdp.mapper")
@SpringBootApplication
public class KbDianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbDianPingApplication.class, args);
    }

}
