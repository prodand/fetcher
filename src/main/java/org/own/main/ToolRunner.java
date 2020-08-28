package org.own.main;

import org.own.main.services.FetcherService;
import org.own.main.services.ParseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableMongoRepositories
public class ToolRunner {

    public static void main(String[] args) {
        SpringApplication.run(ToolRunner.class, args);
    }

    @Bean
    public CommandLineRunner go(FetcherService fetcherService, ParseService parseService) {
        return (args) -> {
//            for (int i = 2009950000; i < 2009952600; i++) {
//                fetcherService.fetchCase("SRC" + i);
//                Thread.sleep(1500);
//            }
            fetcherService.checkExisting();
            parseService.updateDates();
//            System.exit(0);
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
