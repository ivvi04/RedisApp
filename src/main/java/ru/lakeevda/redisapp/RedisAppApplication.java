package ru.lakeevda.redisapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа приложения RedisApp.
 *
 * @author lakeev-da
 */
@SpringBootApplication
public class RedisAppApplication {

    /**
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RedisAppApplication.class, args);
    }
}
