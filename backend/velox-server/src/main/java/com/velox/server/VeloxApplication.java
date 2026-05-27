package com.velox.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
/**
 * 应用启动入口
 */
@SpringBootApplication
public class VeloxApplication {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VeloxApplication.class);

    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        SpringApplication app = new SpringApplication(VeloxApplication.class);
        app.setApplicationStartup(new BufferingApplicationStartup(2048));
        ConfigurableApplicationContext context = app.run(args);

        long cost = System.currentTimeMillis() - start;
        String name = context.getEnvironment().getProperty("spring.application.name", "");
        String port = context.getEnvironment().getProperty("server.port", "");
        String profile = context.getEnvironment().getProperty("spring.profiles.active", "");
        String finishedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String banner = """
        
                ------------------------------------------------------------------------
                START SUCCESS
                ------------------------------------------------------------------------
                Application : {}
                Port        : {}
                Profile     : {}
                GitHub      : https://github.com/VirtualGemini/velox-pro
                ------------------------------------------------------------------------
                Total time  : {} ms
                Finished at : {}
                ------------------------------------------------------------------------
                """;

        log.info(banner, name, port, profile, cost, finishedAt);
    }
}
