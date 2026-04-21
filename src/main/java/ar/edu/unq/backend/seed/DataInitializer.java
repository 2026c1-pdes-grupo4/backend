package ar.edu.unq.backend.seed;


import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepo,
            AgencyRepository agencyRepo,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (userRepo.count() > 0 || agencyRepo.count() > 0) return;

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cth.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setProfileType(ProfileType.ADMIN);
            admin = userRepo.save(admin);

            User buyer = new User();
            buyer.setUsername("buyer1");
            buyer.setEmail("buyer1@cth.com");
            buyer.setPassword(encoder.encode("buyer123"));
            buyer.setProfileType(ProfileType.BUYER);
            userRepo.save(buyer);

            Agency agency = new Agency();
            agency.setUsername("inmo1");
            agency.setEmail("inmo1@cth.com");
            agency.setPassword(encoder.encode("agency123"));
            agency.setAdminUser(admin);
            agencyRepo.save(agency);

            System.out.println("data cargada: admin/admin123, buyer1/buyer123, inmo1/agency123");
        };
    }
}
