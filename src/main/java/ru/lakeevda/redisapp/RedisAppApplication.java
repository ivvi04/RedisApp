package ru.lakeevda.redisapp;

import org.apache.commons.text.AlphabetConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.UserRole;
import ru.lakeevda.redisapp.repository.UserRepository;

import java.time.Instant;

@SpringBootApplication
public class RedisAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisAppApplication.class, args);
    }
}
