package org.sysu.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EntityScan("org.sysu.renCommon.entity")
public class BOEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(BOEngineApplication.class, args);
    }

}

