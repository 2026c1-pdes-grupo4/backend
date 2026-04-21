package ar.edu.unq.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "ar.edu.unq.backend")
@EnableJpaRepositories(basePackages = "ar.edu.unq.backend")
public class CthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CthApplication.class, args);
    }
}

