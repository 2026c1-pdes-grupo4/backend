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

            User admin = new User();
            admin.setUsername("admin123");
            admin.setEmail("admin@cth.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setProfileType(ProfileType.ADMIN);
            admin = userRepository.save(admin);
            //
            User karina = new User();
            karina.setUsername("karina");
            karina.setEmail("karina@argentina.gob");
            karina.setPassword(encoder.encode("admin123"));
            karina.setProfileType(ProfileType.ADMIN);
            karina = userRepository.save(karina);

            User buyer12 = buildBuyer("buyer12", "buyer12@mail.com", encoder);
            buyer12 = userRepository.save(buyer12);
            //
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

            Agency inmo3 = buildAgency("inmo3", "inmo3@propiedades.com", karina, encoder);
            inmo3 = agencyRepository.save(inmo3);
            //
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
            Property house1 = buildProperty(
                    PropertyType.HOUSE, "123 Main Street", "Buenos Aires", "Buenos Aires",
                    3, 85.0, 120000.0, "Bright and spacious house",
                    "1", "A", "10", "5");
            house1 = propertyRepository.save(house1);

            Property apt1 = buildProperty(
                    PropertyType.APARTMENT, "456 Rivadavia Ave", "Córdoba", "Córdoba",
                    2, 55.0, 78000.0, "Modern apartment near the city center",
                    "2", "B", "20", "3");
            apt1 = propertyRepository.save(apt1);

            Property house2 = buildProperty(
                    PropertyType.HOUSE, "789 San Martín Blvd", "Rosario", "Santa Fe",
                    4, 120.0, 190000.0, "Large family home with garden",
                    "3", "C", "30", "7");
            house2 = propertyRepository.save(house2);

            Property apt2 = buildProperty(
                    PropertyType.APARTMENT, "321 Pellegrini St", "La Plata", "Buenos Aires",
                    1, 38.0, 59000.0, "Cozy studio close to university",
                    "4", "D", "40", "2");
            apt2 = propertyRepository.save(apt2);

            Property house3 = buildProperty(
                    PropertyType.HOUSE, "10 Belgrano St", "San Telmo", "Buenos Aires",
                    5, 200.0, 240000.0, "Spacious heritage house in historic neighborhood",
                    "5", "E", "50", "9");
            house3 = propertyRepository.save(house3);
            //
            Property prop1 = buildProperty(
                    PropertyType.APARTMENT, "Miró 548", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 230.0, 230000.0, "semipiso hermoso cuatro ambientes al frente con cochera y baulera", "1", "A", "1", "1");
            prop1 = propertyRepository.save(prop1);

            Property prop2 = buildProperty(
                    PropertyType.HOUSE, "Indio Cua 380", "Exaltación de la Cruz", "Buenos Aires",
                    6, 400.0, 200000.0, "Oportunidad en Indio Cuá Golf Club: Casa con lote de 400 m2 en Exaltación de la Cruz", "2", "B", "3", "7");
            prop2 = propertyRepository.save(prop2);

            Property prop3 = buildProperty(
                    PropertyType.HOUSE, "Barrio La Isla, Lote 45", "Tigre", "Buenos Aires",
                    7, 550.0, 890000.0, "Espectacular casa al lago en el barrio más exclusivo de Nordelta. Terminaciones de categoría.", "3", "C", "5", "12");
            prop3 = propertyRepository.save(prop3);

            Property prop4 = buildProperty(
                    PropertyType.APARTMENT, "Juana Manso 1100", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 120.0, 520000.0, "Exclusivo departamento en piso alto en Puerto Madero. Vista al dique, amenities premium.", "1", "D", "2", "4");
            prop4 = propertyRepository.save(prop4);

            Property prop5 = buildProperty(
                    PropertyType.APARTMENT, "Posadas 1500", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    4, 180.0, 720000.0, "Semipiso de gran categoría en pleno Recoleta. Palier privado, seguridad 24hs.", "4", "A", "8", "15");
            prop5 = propertyRepository.save(prop5);

            Property prop6 = buildProperty(
                    PropertyType.HOUSE, "Estancia Villa Maria, Lote 8", "Ezeiza", "Buenos Aires",
                    5, 320.0, 430000.0, "Casa estilo toscano en el exclusivo club de campo Estancia Villa María. Gran parque.", "2", "E", "6", "9");
            prop6 = propertyRepository.save(prop6);

            Property prop7 = buildProperty(
                    PropertyType.APARTMENT, "Av. Alvear 1800", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    5, 260.0, 950000.0, "Piso barra señorial sobre Av. Alvear. Estilo francés, techos altos, detalles de boiserie.", "1", "B", "4", "3");
            prop7 = propertyRepository.save(prop7);

            Property prop8 = buildProperty(
                    PropertyType.HOUSE, "Barrio Castores, Lote 112", "Tigre", "Buenos Aires",
                    5, 380.0, 580000.0, "Hermosa propiedad desarrollada en dos plantas en Los Castores. Piscina y costa de lago.", "3", "A", "7", "11");
            prop8 = propertyRepository.save(prop8);

            Property prop9 = buildProperty(
                    PropertyType.APARTMENT, "Juncal 2300", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 85.0, 210000.0, "Departamento de 3 ambientes reciclado a nuevo en Barrio Norte. Muy luminoso.", "1", "C", "2", "6");
            prop9.setAvailable(false);
            prop9 = propertyRepository.save(prop9);

            Property prop10 = buildProperty(
                    PropertyType.HOUSE, "Aymonino 300", "Rio Gallegos", "Santa Cruz",
                    4, 160.0, 115000.0, "Casa familiar sólida en zona residencial de Río Gallegos. Entrada para vehículos.", "5", "B", "3", "8");
            prop10 = propertyRepository.save(prop10);

            Property prop11 = buildProperty(
                    PropertyType.HOUSE, "Av. Bustillo Km 4.5", "San Carlos de Bariloche", "Río Negro",
                    5, 290.0, 480000.0, "Propiedad con costa de lago Nahuel Huapi. Vistas panorámicas inigualables.", "6", "A", "5", "2");
            prop11 = propertyRepository.save(prop11);

            Property prop12 = buildProperty(
                    PropertyType.APARTMENT, "Ugarteche 3000", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 110.0, 340000.0, "Excelente departamento en la exclusiva zona de Palermo Nuevo. Balcón corrido.", "1", "D", "1", "9");
            prop12 = propertyRepository.save(prop12);

            Property prop13 = buildProperty(
                    PropertyType.HOUSE, "Club de Campo San Diego, Lote 24", "Moreno", "Buenos Aires",
                    6, 420.0, 390000.0, "Casa de categoría en San Diego Country Club. Amplias comodidades and suite principal.", "4", "C", "6", "4");
            prop13 = propertyRepository.save(prop13);

            Property prop14 = buildProperty(
                    PropertyType.APARTMENT, "Av. Coronel Diaz 2100", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    3, 95.0, 280000.0, "Departamento frente al shopping Alto Palermo. Excelente ubicación y conectividad.", "1", "A", "3", "10");
            prop14 = propertyRepository.save(prop14);

            Property prop15 = buildProperty(
                    PropertyType.APARTMENT, "Martín Coronado 3200", "Ciudad de Buenos Aires", "Ciudad de Buenos Aires",
                    4, 210.0, 850000.0, "Piso exclusivo en torre de categoría en Palermo Chico. Vista despejada al parque.", "2", "B", "5", "6");
            prop15.setAvailable(false);
            prop15 = propertyRepository.save(prop15);

            Property prop16 = buildProperty(
                    PropertyType.HOUSE, "Barrio Highland Park, Los Alamos 45", "Pilar", "Buenos Aires",
                    6, 450.0, 550000.0, "Casa señorial refaccionada en Highland Park. Excelente arboleda y piscina.", "3", "D", "2", "13");
            prop16 = propertyRepository.save(prop16);

            Property prop17 = buildProperty(
                    PropertyType.APARTMENT, "Boulevard Marítimo 2300", "Mar del Plata", "Buenos Aires",
                    3, 70.0, 165000.0, "Departamento con vista plena al mar en edificio céntrico de Mar del Plata.", "1", "E", "4", "7");
            prop17 = propertyRepository.save(prop17);

            Property prop18 = buildProperty(
                    PropertyType.HOUSE, "Ruta 40 Km 2200, Estancia El Desafío", "San Martín de los Andes", "Neuquén",
                    5, 310.0, 720000.0, "Moderna casa de montaña en barrio cerrado con cancha de polo y golf.", "7", "A", "6", "3");
            prop18 = propertyRepository.save(prop18);

            // --- AGENCY PROPERTY LISTINGS ---
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
            //
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

            // --- PICTURES ---
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
            //
            savePictures(pictureRepository, listing6,
                    "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing7,
                    "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1600566753376-12c8ab7fb75b?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing8,
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing9,
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing10,
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing11,
                    "https://images.unsplash.com/photo-1460317442991-0ec209397118?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing12,
                    "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing13,
                    "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing14,
                    "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1552321554-5fefe8c9ef14?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing15,
                    "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing16,
                    "https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing17,
                    "https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1568605114967-8130f3a36994?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing18,
                    "https://images.unsplash.com/photo-1449034446853-66c86144b0ad?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing19,
                    "https://images.unsplash.com/photo-1512915922686-57c11dde9b6b?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing20,
                    "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1600585154526-990dced4db0d?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing21,
                    "https://images.unsplash.com/photo-1567496898669-ee935f5f647a?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=80");
            savePictures(pictureRepository, listing22,
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1512915922686-57c11dde9b6b?auto=format&fit=crop&w=800&q=80");
        
            // --- FAVORITES ---
            saveFavorite(favoriteRepository, buyer1, listing1, 8, "Great location and price");
            saveFavorite(favoriteRepository, buyer1, listing3, 9, "Perfect for a large family");

            saveFavorite(favoriteRepository, buyer2, listing2, 7, "Good value for money");
            saveFavorite(favoriteRepository, buyer2, listing4, 6, "Nice but a bit small");

            /*Favorite fav1 = new Favorite();
            fav1.setUser(manuel);
            fav1.setAgencyProperty(listing1);
            fav1.setScore(5);
            fav1.setComment("Faltan renovaciones, buscar financiamiento");
            fav1.setSavedPrice(340000.0);
            fav1.setSavedDate(LocalDate.of(2025, 4, 1));
            favoriteRepository.save(fav1);*/

            // --- PURCHASES ---
            savePurchase(purchaseRepository, buyer1, listing1, LocalDate.of(2025, 6, 1));
            savePurchase(purchaseRepository, buyer2, listing2, LocalDate.of(2025, 6, 15));

            /*Purchase purchase1 = new Purchase();
            purchase1.setUser(manuel);
            purchase1.setAgencyProperty(listing3);
            purchase1.setPurchasePrice(150000.0);
            purchase1.setPurchaseDate(LocalDate.of(2024, 3, 15));
            purchaseRepository.save(purchase1);*/

            System.out.println("Seed data loaded");
            System.out.println("ADMIN  - admin123 / admin123");
            System.out.println("BUYER1 - buyer1   / buyer123");
            System.out.println("BUYER2 - buyer2   / buyer123");
            System.out.println("AGENCY - inmo1    / agency123");
            System.out.println("AGENCY - inmo2    / agency123");
            System.out.println("BUYER  - buyer12  / buyer123");
            System.out.println("ADMIN  - karina   / admin123");
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
                                   String description, String circumscription,
                                   String section, String block, String parcel) {
        Property p = new Property();
        p.setPropertyType(type);
        p.setAddress(address);
        p.setCity(city);
        p.setProvince(province);
        p.setRooms(rooms);
        p.setAreaSq(area);
        p.setPrice(price);
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
