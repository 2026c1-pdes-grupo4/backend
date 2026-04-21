package ar.edu.unq.backend.seed;


import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.listing.AgencyProperty;
import ar.edu.unq.backend.listing.AgencyPropertyRepository;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyRepository;
import ar.edu.unq.backend.property.PropertyType;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            AgencyRepository agencyRepository,
            PropertyRepository propertyRepository,
            AgencyPropertyRepository agencyPropertyRepository,
            FavoriteRepository favoriteRepository,
            PurchaseRepository purchaseRepository,
            PasswordEncoder encoder

    ) {
        return args -> {
            if (userRepository.count() > 0 || agencyRepository.count() > 0) return;

            User admin = new User();
            admin.setUsername("admin123");
            admin.setEmail("admin@cth.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setProfileType(ProfileType.ADMIN);
            admin = userRepository.save(admin);

            User buyer = new User();
            buyer.setUsername("buyer12");
            buyer.setEmail("buyer1@cth.com");
            buyer.setPassword(encoder.encode("buyer123"));
            buyer.setProfileType(ProfileType.BUYER);
            userRepository.save(buyer);

            Agency agency = new Agency();
            agency.setUsername("inmo3");
            agency.setEmail("inmo1@cth.com");
            agency.setPassword(encoder.encode("agency123"));
            agency.setAdminUser(admin);
            agencyRepository.save(agency);

            Property house = new Property();
            house.setPropertyType(PropertyType.HOUSE);
            house.setPrice(120000.0);
            house.setCity("CABA");
            house.setProvince("Buenos Aires");
            house.setAddress("Av. Calle 123");
            house.setRooms(3);
            house.setAreaSq(85.0);
            house.setDescription("Casa luminosa");
            house.setAvailable(true);
            house = propertyRepository.save(house);

            AgencyProperty listing = new AgencyProperty();
            listing.setAgency(agency);
            listing.setProperty(house);
            listing.setListedPrice(115000.0);
            listing.setListedDate(LocalDate.now());
            listing.setActive(true);
            listing = agencyPropertyRepository.save(listing);

            Favorite favorite = new Favorite();
            favorite.setUser(buyer);
            favorite.setProperty(house);
            favorite.setSavedDate(LocalDate.now());
            favorite.setSavedPrice(house.getPrice());
            favorite.setScore(8);
            favorite.setComment("Buena ubicación y precio");
            favoriteRepository.save(favorite);

            Purchase purchase = new Purchase();
            purchase.setUser(buyer);
            purchase.setAgencyProperty(listing);
            purchase.setPurchasePrice(listing.getListedPrice());
            purchase.setPurchaseDate(LocalDate.now());
            purchaseRepository.save(purchase);

            System.out.println("data cargada");
        };
    }
}
