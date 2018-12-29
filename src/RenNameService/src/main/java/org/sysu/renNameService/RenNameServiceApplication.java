/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Ren Name Service Entry Point.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EntityScan("org.sysu.renCommon.entity")
public class RenNameServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenNameServiceApplication.class, args);
    }

}
