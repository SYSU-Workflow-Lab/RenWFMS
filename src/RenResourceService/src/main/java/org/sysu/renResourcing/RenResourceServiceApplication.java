/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Ren Resource Service Entry Point.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EntityScan("org.sysu.renCommon.entity")
@EnableScheduling
public class RenResourceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenResourceServiceApplication.class, args);
    }
}
