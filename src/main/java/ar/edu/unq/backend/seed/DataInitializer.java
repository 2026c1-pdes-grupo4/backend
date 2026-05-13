package ar.edu.unq.backend.seed;


import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.picture.Picture;
import ar.edu.unq.backend.picture.PictureRepository;
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
            PictureRepository pictureRepository,
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

            User buyer1 = new User();
            buyer1.setUsername("buyer1");
            buyer1.setEmail("buyer1@cth.com");
            buyer1.setPassword(encoder.encode("buyer123"));
            buyer1.setProfileType(ProfileType.BUYER);
            buyer1 = userRepository.save(buyer1);

            User buyer2 = new User();
            buyer2.setUsername("buyer2");
            buyer2.setEmail("buyer2@cth.com");
            buyer2.setPassword(encoder.encode("buyer123"));
            buyer2.setProfileType(ProfileType.BUYER);
            buyer2 = userRepository.save(buyer2);

            Agency inmo1 = new Agency();
            inmo1.setUsername("inmo1");
            inmo1.setEmail("inmo1@cth.com");
            inmo1.setPassword(encoder.encode("agency123"));
            inmo1.setAdminUser(admin);
            inmo1 = agencyRepository.save(inmo1);

            Agency inmo2 = new Agency();
            inmo2.setUsername("inmo2");
            inmo2.setEmail("inmo2@cth.com");
            inmo2.setPassword(encoder.encode("agency123"));
            inmo2.setAdminUser(admin);
            inmo2 = agencyRepository.save(inmo2);

            Property house1 = buildProperty(
                    PropertyType.HOUSE, "123 Main Street", "Buenos Aires", "Buenos Aires",
                    3, 85.0, "Bright and spacious house",
                    "1", "A", "10", "5");
            house1 = propertyRepository.save(house1);

            Property apt1 = buildProperty(
                    PropertyType.APARTMENT, "456 Rivadavia Ave", "Córdoba", "Córdoba",
                    2, 55.0, "Modern apartment near the city center",
                    "2", "B", "20", "3");
            apt1 = propertyRepository.save(apt1);

            Property house2 = buildProperty(
                    PropertyType.HOUSE, "789 San Martín Blvd", "Rosario", "Santa Fe",
                    4, 120.0, "Large family home with garden",
                    "3", "C", "30", "7");
            house2 = propertyRepository.save(house2);

            Property apt2 = buildProperty(
                    PropertyType.APARTMENT, "321 Pellegrini St", "La Plata", "Buenos Aires",
                    1, 38.0, "Cozy studio close to university",
                    "4", "D", "40", "2");
            apt2 = propertyRepository.save(apt2);

            Property house3 = buildProperty(
                    PropertyType.HOUSE, "10 Belgrano St", "San Telmo", "Buenos Aires",
                    5, 200.0, "Spacious heritage house in historic neighborhood",
                    "5", "E", "50", "9");
            house3 = propertyRepository.save(house3);

            AgencyProperty listing1 = buildListing(inmo1, house1, 115000.0, LocalDate.of(2025, 1, 15));
            listing1 = agencyPropertyRepository.save(listing1);

            AgencyProperty listing2 = buildListing(inmo1, apt1, 75000.0, LocalDate.of(2025, 2, 1));
            listing2 = agencyPropertyRepository.save(listing2);

            AgencyProperty listing3 = buildListing(inmo2, house2, 185000.0, LocalDate.of(2025, 3, 10));
            listing3 = agencyPropertyRepository.save(listing3);

            AgencyProperty listing4 = buildListing(inmo2, apt2, 55000.0, LocalDate.of(2025, 4, 5));
            listing4 = agencyPropertyRepository.save(listing4);

            AgencyProperty listing5 = buildListing(inmo1, house3, 230000.0, LocalDate.of(2025, 5, 20));
            listing5 = agencyPropertyRepository.save(listing5);

            savePictures(pictureRepository, listing1,
                    "https://images.unsplash.com/photo-1568605114967-8130f3a36994",
                    "https://images.unsplash.com/photo-1570129477492-45c003edd2be");

            savePictures(pictureRepository, listing2,
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267",
                    "https://images.unsplash.com/photo-1493809842364-78817add7ffb");

            savePictures(pictureRepository, listing3,
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                    "https://images.unsplash.com/photo-1512917774080-9991f1c4c750");

            savePictures(pictureRepository, listing4,
                    "https://images.unsplash.com/photo-1554995207-c18c203602cb",
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2");

            savePictures(pictureRepository, listing5,
                    "https://images.unsplash.com/photo-1605276373954-0c4a0dac5b12",
                    "https://images.unsplash.com/photo-1582268611958-ebfd161ef9cf");

            saveFavorite(favoriteRepository, buyer1, listing1, 8, "Great location and price");
            saveFavorite(favoriteRepository, buyer1, listing3, 9, "Perfect for a large family");

            saveFavorite(favoriteRepository, buyer2, listing2, 7, "Good value for money");
            saveFavorite(favoriteRepository, buyer2, listing4, 6, "Nice but a bit small");

            savePurchase(purchaseRepository, buyer1, listing1, LocalDate.of(2025, 6, 1));

            savePurchase(purchaseRepository, buyer2, listing2, LocalDate.of(2025, 6, 15));

            System.out.println("Seed data loaded");
            System.out.println("ADMIN  - admin123 / admin123");
            System.out.println("BUYER1 - buyer1   / buyer123");
            System.out.println("BUYER2 - buyer2   / buyer123");
            System.out.println("AGENCY - inmo1    / agency123");
            System.out.println("AGENCY - inmo2    / agency123");
        };
    }

    // Helpers

    private Property buildProperty(PropertyType type, String address, String city,
                                   String province, int rooms, double area,
                                   String description, String circumscription,
                                   String section, String block, String parcel) {
        Property p = new Property();
        p.setPropertyType(type);
        p.setAddress(address);
        p.setCity(city);
        p.setProvince(province);
        p.setRooms(rooms);
        p.setAreaSq(area);
        p.setDescription(description);
        p.setAvailable(true);
        p.setCircumscription(circumscription);
        p.setSection(section);
        p.setBlock(block);
        p.setParcel(parcel);
        return p;
    }

    private AgencyProperty buildListing(Agency agency, Property property,
                                        double price, LocalDate date) {
        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(agency);
        ap.setProperty(property);
        ap.setListedPrice(price);
        ap.setListedDate(date);
        return ap;
    }

    private void savePictures(PictureRepository repo, AgencyProperty listing,
                              String url1, String url2) {
        Picture p1 = new Picture();
        p1.setAgencyProperty(listing);
        p1.setUrl(url1);
        repo.save(p1);

        Picture p2 = new Picture();
        p2.setAgencyProperty(listing);
        p2.setUrl(url2);
        repo.save(p2);
    }

    private void saveFavorite(FavoriteRepository repo, User user,
                              AgencyProperty listing, int score, String comment) {
        Favorite f = new Favorite();
        f.setUser(user);
        f.setAgencyProperty(listing);
        f.setSavedDate(LocalDate.now());
        f.setSavedPrice(listing.getListedPrice());
        f.setScore(score);
        f.setComment(comment);
        repo.save(f);
    }

    private void savePurchase(PurchaseRepository repo, User user,
                              AgencyProperty listing, LocalDate date) {
        Purchase p = new Purchase();
        p.setUser(user);
        p.setAgencyProperty(listing);
        p.setPurchasePrice(listing.getListedPrice());
        p.setPurchaseDate(date);
        repo.save(p);
    }
}
