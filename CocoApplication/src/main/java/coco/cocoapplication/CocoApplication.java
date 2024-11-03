package coco.cocoapplication;

import coco.cocoapplication.model.Connection;
import coco.cocoapplication.model.Customer;
import coco.cocoapplication.model.Refinery;
import coco.cocoapplication.model.Tank;
import coco.cocoapplication.service.CsvParserService;
import coco.cocoapplication.service.impl.GreedyServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CocoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CocoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner run(CsvParserService csvParserService, GreedyServiceImpl greedyService) {
        return args -> {
            greedyService.refineries = csvParserService.parseCsv("refineries.csv", Refinery.class);
            greedyService.tanks = csvParserService.parseCsv("tanks.csv", Tank.class);
            greedyService.customers = csvParserService.parseCsv("customers.csv", Customer.class);
            greedyService.connections = csvParserService.parseCsv("connections.csv", Connection.class);

            greedyService.precalculate();
        };
    }
}
