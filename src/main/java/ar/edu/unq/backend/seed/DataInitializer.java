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

            // --- USERS ---

            User karina = new User();
            karina.setUsername("karina");
            karina.setEmail("karina@argentina.gob");
            karina.setPassword(encoder.encode("admin123"));
            karina.setProfileType(ProfileType.ADMIN);
            karina = userRepository.save(karina);

            User admin = new User();
            admin.setUsername("admin123");
            admin.setEmail("admin@cth.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setProfileType(ProfileType.ADMIN);
            admin = userRepository.save(admin);

            User buyer12 = buildBuyer("buyer12", "buyer12@mail.com", encoder);
            buyer12 = userRepository.save(buyer12);

            User manuel = buildBuyer("manuel", "manuel@argentina.gob", encoder);
            manuel = userRepository.save(manuel);

            User claudia = buildBuyer("claudia", "claudia@mail.com", encoder);
            claudia = userRepository.save(claudia);

            User jorge = buildBuyer("jorge", "jorge@mail.com", encoder);
            jorge = userRepository.save(jorge);

            User patricia = buildBuyer("patricia", "patricia@mail.com", encoder);
            patricia = userRepository.save(patricia);

            User roberto = buildBuyer("roberto", "roberto@mail.com", encoder);
            roberto = userRepository.save(roberto);

            User ana = buildBuyer("ana", "ana@mail.com", encoder);
            ana = userRepository.save(ana);

            User diego = buildBuyer("diego", "diego@mail.com", encoder);
            diego = userRepository.save(diego);

            User laura = buildBuyer("laura", "laura@mail.com", encoder);
            laura = userRepository.save(laura);

            User martin = buildBuyer("martin", "martin@mail.com", encoder);
            martin = userRepository.save(martin);

            User sofia = buildBuyer("sofia", "sofia@mail.com", encoder);
            sofia = userRepository.save(sofia);

            User lucas = buildBuyer("lucas", "lucas@mail.com", encoder);
            lucas = userRepository.save(lucas);

            User valentina = buildBuyer("valentina", "valentina@mail.com", encoder);
            valentina = userRepository.save(valentina);

            User nicolas = buildBuyer("nicolas", "nicolas@mail.com", encoder);
            nicolas = userRepository.save(nicolas);

            User camila = buildBuyer("camila", "camila@mail.com", encoder);
            camila = userRepository.save(camila);

            // --- AGENCIES (all owned by karina) ---

            Agency inmo3 = buildAgency("inmo3", "inmo3@propiedades.com", karina, encoder);
            inmo3 = agencyRepository.save(inmo3);

            Agency ritondo = buildAgency("ritondo_propiedades", "ritondo@propiedades.com", karina, encoder);
            ritondo = agencyRepository.save(ritondo);

            Agency nordelta = buildAgency("nordelta_propiedades", "contacto@nordeltapropiedades.com", karina, encoder);
            nordelta = agencyRepository.save(nordelta);

            Agency puertoMadero = buildAgency("puerto_madero_brokers", "info@pmbrokers.com", karina, encoder);
            puertoMadero = agencyRepository.save(puertoMadero);

            Agency surInversiones = buildAgency("sur_inversiones", "ventas@surinversiones.com", karina, encoder);
            surInversiones = agencyRepository.save(surInversiones);

            Agency agroBienes = buildAgency("agro_bienes_raices", "campo@agrobienesraices.com", karina, encoder);
            agroBienes = agencyRepository.save(agroBienes);

            Agency patagonia = buildAgency("patagonia_bienes", "sur@patagoniabienes.com", karina, encoder);
            patagonia = agencyRepository.save(patagonia);

            // --- PROPERTIES ---

            Property prop1 = buildProperty(
                    PropertyType.APARTMENT, "Miró 548", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 230.0, 230000.0, "semipiso hermoso cuatro ambientes al frente con cochera y baulera", true);
            prop1 = propertyRepository.save(prop1);

            Property prop2 = buildProperty(
                    PropertyType.HOUSE, "Indio Cua 380", "Exaltación de la Cruz", "Buenos Aires",
                    6, 400.0, 200000.0, "Oportunidad en Indio Cuá Golf Club: Casa con lote de 400 m2 en Exaltación de la Cruz", true);
            prop2 = propertyRepository.save(prop2);

            Property prop3 = buildProperty(
                    PropertyType.HOUSE, "Barrio La Isla, Lote 45", "Tigre", "Buenos Aires",
                    7, 550.0, 890000.0, "Espectacular casa al lago en el barrio más exclusivo de Nordelta. Terminaciones de categoría.", true);
            prop3 = propertyRepository.save(prop3);

            Property prop4 = buildProperty(
                    PropertyType.APARTMENT, "Juana Manso 1100", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 120.0, 520000.0, "Exclusivo departamento en piso alto en Puerto Madero. Vista al dique, amenities premium.", true);
            prop4 = propertyRepository.save(prop4);

            Property prop5 = buildProperty(
                    PropertyType.APARTMENT, "Posadas 1500", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    4, 180.0, 720000.0, "Semipiso de gran categoría en pleno Recoleta. Palier privado, seguridad 24hs.", true);
            prop5 = propertyRepository.save(prop5);

            Property prop6 = buildProperty(
                    PropertyType.HOUSE, "Estancia Villa Maria, Lote 8", "Ezeiza", "Buenos Aires",
                    5, 320.0, 430000.0, "Casa estilo toscano en el exclusivo club de campo Estancia Villa María. Gran parque.", true);
            prop6 = propertyRepository.save(prop6);

            Property prop7 = buildProperty(
                    PropertyType.APARTMENT, "Av. Alvear 1800", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    5, 260.0, 950000.0, "Piso barra señorial sobre Av. Alvear. Estilo francés, techos altos, detalles de boiserie.", true);
            prop7 = propertyRepository.save(prop7);

            Property prop8 = buildProperty(
                    PropertyType.HOUSE, "Barrio Castores, Lote 112", "Tigre", "Buenos Aires",
                    5, 380.0, 580000.0, "Hermosa propiedad desarrollada en dos plantas en Los Castores. Piscina y costa de lago.", true);
            prop8 = propertyRepository.save(prop8);

            Property prop9 = buildProperty(
                    PropertyType.APARTMENT, "Juncal 2300", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 85.0, 210000.0, "Departamento de 3 ambientes reciclado a nuevo en Barrio Norte. Muy luminoso.", false);
            prop9 = propertyRepository.save(prop9);

            Property prop10 = buildProperty(
                    PropertyType.HOUSE, "Aymonino 300", "Rio Gallegos", "Santa Cruz",
                    4, 160.0, 115000.0, "Casa familiar sólida en zona residencial de Río Gallegos. Entrada para vehículos.", true);
            prop10 = propertyRepository.save(prop10);

            Property prop11 = buildProperty(
                    PropertyType.HOUSE, "Av. Bustillo Km 4.5", "San Carlos de Bariloche", "Río Negro",
                    5, 290.0, 480000.0, "Propiedad con costa de lago Nahuel Huapi. Vistas panorámicas inigualables.", true);
            prop11 = propertyRepository.save(prop11);

            Property prop12 = buildProperty(
                    PropertyType.APARTMENT, "Ugarteche 3000", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 110.0, 340000.0, "Excelente departamento en la exclusiva zona de Palermo Nuevo. Balcón corrido.", true);
            prop12 = propertyRepository.save(prop12);

            Property prop13 = buildProperty(
                    PropertyType.HOUSE, "Club de Campo San Diego, Lote 24", "Moreno", "Buenos Aires",
                    6, 420.0, 390000.0, "Casa de categoría en San Diego Country Club. Amplias comodidades and suite principal.", true);
            prop13 = propertyRepository.save(prop13);

            Property prop14 = buildProperty(
                    PropertyType.APARTMENT, "Av. Coronel Diaz 2100", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 95.0, 280000.0, "Departamento frente al shopping Alto Palermo. Excelente ubicación y conectividad.", true);
            prop14 = propertyRepository.save(prop14);

            Property prop15 = buildProperty(
                    PropertyType.APARTMENT, "Martín Coronado 3200", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    4, 210.0, 850000.0, "Piso exclusivo en torre de categoría en Palermo Chico. Vista despejada al parque.", false);
            prop15 = propertyRepository.save(prop15);

            Property prop16 = buildProperty(
                    PropertyType.HOUSE, "Barrio Highland Park, Los Alamos 45", "Pilar", "Buenos Aires",
                    6, 450.0, 550000.0, "Casa señorial refaccionada en Highland Park. Excelente arboleda y piscina.", true);
            prop16 = propertyRepository.save(prop16);

            Property prop17 = buildProperty(
                    PropertyType.APARTMENT, "Boulevard Marítimo 2300", "Mar del Plata", "Buenos Aires",
                    3, 70.0, 165000.0, "Departamento con vista plena al mar en edificio céntrico de Mar del Plata.", true);
            prop17 = propertyRepository.save(prop17);

            Property prop18 = buildProperty(
                    PropertyType.HOUSE, "Ruta 40 Km 2200, Estancia El Desafío", "San Martín de los Andes", "Neuquén",
                    5, 310.0, 720000.0, "Moderna casa de montaña en barrio cerrado con cancha de polo y golf.", true);
            prop18 = propertyRepository.save(prop18);

            // --- AGENCY PROPERTY LISTINGS ---

            AgencyProperty listing1 = buildListing(ritondo, prop1, 230000.0, LocalDate.of(2025, 1, 1));
            listing1 = agencyPropertyRepository.save(listing1);

            AgencyProperty listing2 = buildListing(puertoMadero, prop1, 245000.0, LocalDate.of(2025, 2, 15));
            listing2 = agencyPropertyRepository.save(listing2);

            AgencyProperty listing3 = buildListing(ritondo, prop2, 200000.0, LocalDate.of(2025, 1, 15));
            listing3 = agencyPropertyRepository.save(listing3);

            AgencyProperty listing4 = buildListing(ritondo, prop3, 890000.0, LocalDate.of(2026, 1, 2));
            listing4 = agencyPropertyRepository.save(listing4);

            AgencyProperty listing5 = buildListing(nordelta, prop3, 900000.0, LocalDate.of(2025, 2, 2));
            listing5 = agencyPropertyRepository.save(listing5);

            AgencyProperty listing6 = buildListing(puertoMadero, prop7, 950000.0, LocalDate.of(2025, 3, 5));
            listing6 = agencyPropertyRepository.save(listing6);

            AgencyProperty listing7 = buildListing(ritondo, prop7, 920000.0, LocalDate.of(2025, 4, 1));
            listing7 = agencyPropertyRepository.save(listing7);

            AgencyProperty listing8 = buildListing(surInversiones, prop13, 390000.0, LocalDate.of(2025, 4, 20));
            listing8 = agencyPropertyRepository.save(listing8);

            AgencyProperty listing9 = buildListing(agroBienes, prop13, 410000.0, LocalDate.of(2025, 5, 2));
            listing9 = agencyPropertyRepository.save(listing9);

            AgencyProperty listing10 = buildListing(ritondo, prop9, 210000.0, LocalDate.of(2025, 3, 22));
            listing10 = agencyPropertyRepository.save(listing10);

            AgencyProperty listing11 = buildListing(ritondo, prop12, 340000.0, LocalDate.of(2025, 4, 15));
            listing11 = agencyPropertyRepository.save(listing11);

            AgencyProperty listing12 = buildListing(ritondo, prop14, 280000.0, LocalDate.of(2025, 5, 14));
            listing12 = agencyPropertyRepository.save(listing12);

            AgencyProperty listing13 = buildListing(puertoMadero, prop4, 520000.0, LocalDate.of(2025, 2, 11));
            listing13 = agencyPropertyRepository.save(listing13);

            AgencyProperty listing14 = buildListing(puertoMadero, prop5, 720000.0, LocalDate.of(2025, 2, 25));
            listing14 = agencyPropertyRepository.save(listing14);

            AgencyProperty listing15 = buildListing(surInversiones, prop6, 430000.0, LocalDate.of(2025, 3, 1));
            listing15 = agencyPropertyRepository.save(listing15);

            AgencyProperty listing16 = buildListing(nordelta, prop8, 580000.0, LocalDate.of(2025, 3, 18));
            listing16 = agencyPropertyRepository.save(listing16);

            AgencyProperty listing17 = buildListing(patagonia, prop10, 115000.0, LocalDate.of(2025, 4, 2));
            listing17 = agencyPropertyRepository.save(listing17);

            AgencyProperty listing18 = buildListing(patagonia, prop11, 480000.0, LocalDate.of(2025, 4, 9));
            listing18 = agencyPropertyRepository.save(listing18);

            AgencyProperty listing19 = buildListing(puertoMadero, prop15, 850000.0, LocalDate.of(2025, 5, 28));
            listing19 = agencyPropertyRepository.save(listing19);

            AgencyProperty listing20 = buildListing(nordelta, prop16, 550000.0, LocalDate.of(2025, 6, 2));
            listing20 = agencyPropertyRepository.save(listing20);

            AgencyProperty listing21 = buildListing(surInversiones, prop17, 165000.0, LocalDate.of(2025, 6, 10));
            listing21 = agencyPropertyRepository.save(listing21);

            AgencyProperty listing22 = buildListing(patagonia, prop18, 720000.0, LocalDate.of(2025, 6, 15));
            listing22 = agencyPropertyRepository.save(listing22);

            // --- PICTURES (one per listing, from fixture imageUrl) ---

            savePicture(pictureRepository, listing1, "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing2, "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing3, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing4, "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing5, "https://images.unsplash.com/photo-1613490493576-7fde63acd811?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing6, "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing7, "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing8, "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing9, "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing10, "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing11, "https://images.unsplash.com/photo-1460317442991-0ec209397118?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing12, "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing13, "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing14, "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing15, "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing16, "https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing17, "https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing18, "https://images.unsplash.com/photo-1449034446853-66c86144b0ad?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing19, "https://images.unsplash.com/photo-1512915922686-57c11dde9b6b?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing20, "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing21, "https://images.unsplash.com/photo-1567496898669-ee935f5f647a?auto=format&fit=crop&w=800&q=80");
            savePicture(pictureRepository, listing22, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");

            // --- FAVORITES ---

            Favorite fav1 = new Favorite();
            fav1.setUser(manuel);
            fav1.setAgencyProperty(listing1);
            fav1.setScore(5);
            fav1.setComment("Faltan renovaciones, buscar financiamiento");
            fav1.setSavedPrice(340000.0);
            fav1.setSavedDate(LocalDate.of(2025, 4, 1));
            favoriteRepository.save(fav1);

            // --- PURCHASES ---

            Purchase purchase1 = new Purchase();
            purchase1.setUser(manuel);
            purchase1.setAgencyProperty(listing3);
            purchase1.setPurchasePrice(150000.0);
            purchase1.setPurchaseDate(LocalDate.of(2024, 3, 15));
            purchaseRepository.save(purchase1);

            System.out.println("Seed data loaded");
            System.out.println("BUYER  - buyer12  / buyer123");
            System.out.println("ADMIN  - karina   / admin123");
            System.out.println("ADMIN  - admin123 / admin123");
            System.out.println("BUYER  - manuel   / buyer123");
            System.out.println("AGENCY - inmo3                  / agency123");
            System.out.println("AGENCY - ritondo_propiedades    / agency123");
            System.out.println("AGENCY - nordelta_propiedades   / agency123");
            System.out.println("AGENCY - puerto_madero_brokers  / agency123");
            System.out.println("AGENCY - sur_inversiones        / agency123");
            System.out.println("AGENCY - agro_bienes_raices     / agency123");
            System.out.println("AGENCY - patagonia_bienes       / agency123");
        };
    }

    // Helpers

    private User buildBuyer(String username, String email, PasswordEncoder encoder) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode("buyer123"));
        u.setProfileType(ProfileType.BUYER);
        return u;
    }

    private Agency buildAgency(String username, String email, User adminUser, PasswordEncoder encoder) {
        Agency a = new Agency();
        a.setUsername(username);
        a.setEmail(email);
        a.setPassword(encoder.encode("agency123"));
        a.setAdminUser(adminUser);
        return a;
    }

    private Property buildProperty(PropertyType type, String address, String city,
                                   String province, int rooms, double area, double price,
                                   String description, boolean available) {
        Property p = new Property();
        p.setPropertyType(type);
        p.setAddress(address);
        p.setCity(city);
        p.setProvince(province);
        p.setRooms(rooms);
        p.setAreaSq(area);
        p.setPrice(price);
        p.setDescription(description);
        p.setAvailable(available);
        p.setCircumscription("1");
        p.setSection("A");
        p.setBlock("1");
        p.setParcel("1");
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

    private void savePicture(PictureRepository repo, AgencyProperty listing, String url) {
        Picture p = new Picture();
        p.setAgencyProperty(listing);
        p.setUrl(url);
        repo.save(p);
    }
}
