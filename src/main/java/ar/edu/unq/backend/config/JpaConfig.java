package ar.edu.unq.backend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración JPA separada de la clase principal para que @WebMvcTest
 * no intente cargar la infraestructura de JPA en los tests de capa web.
 */
@Configuration
@EntityScan(basePackages = "ar.edu.unq.backend")
@EnableJpaRepositories(basePackages = "ar.edu.unq.backend")
public class JpaConfig {
}

