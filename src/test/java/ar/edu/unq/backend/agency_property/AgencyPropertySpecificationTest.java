package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyType;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de la Specification usando @DataJpaTest (H2 en memoria).
 * Cada test corre en una transacción que se revierte al finalizar,
 * por lo que la BD queda limpia entre tests.
 */
@DataJpaTest
class AgencyPropertySpecificationTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    AgencyPropertyRepository repository;

    Agency agencyAlpha;
    Agency agencyBeta;

    @BeforeEach
    void setUp() {
        User admin = em.persist(user("admin", "admin@test.com"));
        agencyAlpha = em.persist(agency("alpha_inmobiliaria", admin));
        agencyBeta  = em.persist(agency("beta_inmobiliaria",  admin));
        em.flush();
    }

    @Test
    void returnsOnlyActiveNonDeletedListings() {
        Property active = em.persist(prop("Calle 1", "CABA", "CABA", 3, 100_000.0, true));
        Property sold   = em.persist(prop("Calle 2", "CABA", "CABA", 3, 100_000.0, false));
        Property del    = em.persist(prop("Calle 3", "CABA", "CABA", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, active, 100_000.0, false)); // ✓
        em.persist(listing(agencyAlpha, sold,   100_000.0, false)); // ✗ sold
        em.persist(listing(agencyAlpha, del,    100_000.0, true));  // ✗ deleted listing
        em.flush();

        List<AgencyProperty> content = search(noFilters()).getContent();

        assertThat(content).hasSize(1);
        assertThat(content.get(0).getProperty().getAddress()).isEqualTo("Calle 1");
    }

    @Test
    void excludes_deletedListings() {
        Property p = em.persist(prop("Del A", "CABA", "CABA", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, p, 100_000.0, true));
        em.flush();

        assertThat(search(noFilters()).getContent()).isEmpty();
    }

    @Test
    void excludes_soldProperties() {
        Property sold = em.persist(prop("Sold B", "CABA", "CABA", 3, 100_000.0, false));
        em.persist(listing(agencyAlpha, sold, 100_000.0, false));
        em.flush();

        assertThat(search(noFilters()).getContent()).isEmpty();
    }

    @Test
    void roomsMin_excludes_listingsWithFewerRooms() {
        Property small = em.persist(prop("RA1", "CABA", "CABA", 3,  80_000.0, true));
        Property big   = em.persist(prop("RA2", "CABA", "CABA", 5, 200_000.0, true));
        em.persist(listing(agencyAlpha, small,  80_000.0, false));
        em.persist(listing(agencyAlpha, big,   200_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, null, 5, null, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getRooms()).isEqualTo(5);
    }

    @Test
    void roomsMax_excludes_listingsWithMoreRooms() {
        Property small = em.persist(prop("RB1", "CABA", "CABA", 2,  50_000.0, true));
        Property big   = em.persist(prop("RB2", "CABA", "CABA", 6, 400_000.0, true));
        em.persist(listing(agencyAlpha, small,  50_000.0, false));
        em.persist(listing(agencyAlpha, big,   400_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, null, null, 3, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getRooms()).isEqualTo(2);
    }

    @Test
    void roomsMinAndMax_filtersRange() {
        Property r2 = em.persist(prop("RC1", "CABA", "CABA", 2,  50_000.0, true));
        Property r4 = em.persist(prop("RC2", "CABA", "CABA", 4, 120_000.0, true));
        Property r7 = em.persist(prop("RC3", "CABA", "CABA", 7, 300_000.0, true));
        em.persist(listing(agencyAlpha, r2,  50_000.0, false));
        em.persist(listing(agencyAlpha, r4, 120_000.0, false));
        em.persist(listing(agencyAlpha, r7, 300_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, null, 3, 5, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getRooms()).isEqualTo(4);
    }

    @Test
    void city_partialMatch_caseInsensitive() {
        Property bsas    = em.persist(prop("DA1", "Buenos Aires", "Buenos Aires", 3, 100_000.0, true));
        Property rosario = em.persist(prop("DA2", "Rosario",      "Santa Fe",     3, 100_000.0, true));
        em.persist(listing(agencyAlpha, bsas,    100_000.0, false));
        em.persist(listing(agencyAlpha, rosario, 100_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec("BUENOS", null, null, null, null, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getCity()).isEqualTo("Buenos Aires");
    }

    @Test
    void province_partialMatch_caseInsensitive() {
        Property bsas = em.persist(prop("EA1", "CABA",       "Buenos Aires", 3, 100_000.0, true));
        Property sc   = em.persist(prop("EA2", "Bariloche",  "Santa Cruz",   3, 100_000.0, true));
        em.persist(listing(agencyAlpha, bsas, 100_000.0, false));
        em.persist(listing(agencyAlpha, sc,   100_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, "SANTA", null, null, null, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getProvince()).isEqualTo("Santa Cruz");
    }


    @Test
    void propertyType_exactMatch() {
        Property house = em.persist(typedProp(PropertyType.HOUSE,     "FA1", "CABA", "CABA", 3, 100_000.0, true));
        Property apt   = em.persist(typedProp(PropertyType.APARTMENT, "FA2", "CABA", "CABA", 3,  80_000.0, true));
        em.persist(listing(agencyAlpha, house, 100_000.0, false));
        em.persist(listing(agencyAlpha, apt,    80_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, PropertyType.APARTMENT, null, null, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getPropertyType())
                .isEqualTo(PropertyType.APARTMENT);
    }

    @Test
    void priceMin_excludes_listingsBelowThreshold() {
        Property cheap  = em.persist(prop("GA1", "CABA", "CABA", 2,  50_000.0, true));
        Property pricey = em.persist(prop("GA2", "CABA", "CABA", 3, 200_000.0, true));
        em.persist(listing(agencyAlpha, cheap,   50_000.0, false));
        em.persist(listing(agencyAlpha, pricey, 200_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, null, null, null, 100_000.0, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListedPrice()).isEqualTo(200_000.0);
    }

    @Test
    void priceMax_excludes_listingsAboveThreshold() {
        Property cheap  = em.persist(prop("HA1", "CABA", "CABA", 2,  50_000.0, true));
        Property pricey = em.persist(prop("HA2", "CABA", "CABA", 3, 300_000.0, true));
        em.persist(listing(agencyAlpha, cheap,   50_000.0, false));
        em.persist(listing(agencyAlpha, pricey, 300_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec(null, null, null, null, null, null, 100_000.0, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getListedPrice()).isEqualTo(50_000.0);
    }

    @Test
    void keyword_matchesAddress() {
        Property p = em.persist(prop("Av. Libertador 1500", "CABA", "CABA", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, p, 100_000.0, false));
        em.flush();

        assertThat(search(spec(null, null, null, null, null, null, null, "libertador"))
                .getContent()).hasSize(1);
    }

    @Test
    void keyword_matchesDescription() {
        Property p = prop("KA1", "CABA", "CABA", 3, 100_000.0, true);
        p.setDescription("luminoso y amplio depto");
        em.persist(p);
        em.persist(listing(agencyAlpha, p, 100_000.0, false));
        em.flush();

        assertThat(search(spec(null, null, null, null, null, null, null, "LUMINOSO"))
                .getContent()).hasSize(1);
    }

    @Test
    void keyword_matchesCity() {
        Property p = em.persist(prop("KB1", "Tigre", "Buenos Aires", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, p, 100_000.0, false));
        em.flush();

        assertThat(search(spec(null, null, null, null, null, null, null, "tigre"))
                .getContent()).hasSize(1);
    }

    @Test
    void keyword_matchesProvince() {
        Property p = em.persist(prop("KC1", "Bariloche", "Río Negro", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, p, 100_000.0, false));
        em.flush();

        assertThat(search(spec(null, null, null, null, null, null, null, "negro"))
                .getContent()).hasSize(1);
    }

    @Test
    void keyword_noMatch_returnsEmpty() {
        Property p = em.persist(prop("KD1", "CABA", "CABA", 3, 100_000.0, true));
        em.persist(listing(agencyAlpha, p, 100_000.0, false));
        em.flush();

        assertThat(search(spec(null, null, null, null, null, null, null, "xyzNoMatch"))
                .getContent()).isEmpty();
    }

    @Test
    void multipleFilters_appliedWithAndLogic() {
        // ✓ Cumple todos: HOUSE, city~Buenos, rooms=4
        Property match     = em.persist(typedProp(PropertyType.HOUSE,     "Palermo 100", "Buenos Aires", "Buenos Aires", 4, 150_000.0, true));
        // ✗ Tipo incorrecto
        Property wrongType = em.persist(typedProp(PropertyType.APARTMENT, "Palermo 200", "Buenos Aires", "Buenos Aires", 4, 150_000.0, true));
        // ✗ Pocas habitaciones
        Property fewRooms  = em.persist(typedProp(PropertyType.HOUSE,     "Palermo 300", "Buenos Aires", "Buenos Aires", 1, 150_000.0, true));
        em.persist(listing(agencyAlpha, match,     150_000.0, false));
        em.persist(listing(agencyAlpha, wrongType, 150_000.0, false));
        em.persist(listing(agencyAlpha, fewRooms,  150_000.0, false));
        em.flush();

        Page<AgencyProperty> result = search(spec("Buenos", null, PropertyType.HOUSE, 3, null, null, null, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProperty().getAddress()).isEqualTo("Palermo 100");
    }

    @Test
    void results_orderedByAddress_thenByAgencyUsername() {
        Property pA = em.persist(prop("Calle A", "CABA", "CABA", 3, 100_000.0, true));
        Property pB = em.persist(prop("Calle B", "CABA", "CABA", 3, 100_000.0, true));
        // Misma dirección pA → dos agencias → se ordena por username
        em.persist(listing(agencyBeta,  pA, 100_000.0, false)); // beta > alpha
        em.persist(listing(agencyAlpha, pA, 100_000.0, false)); // alpha
        em.persist(listing(agencyAlpha, pB, 100_000.0, false));
        em.flush();

        List<AgencyProperty> content = search(noFilters()).getContent();

        assertThat(content).hasSize(3);
        // [0] "Calle A" / "alpha_inmobiliaria"
        assertThat(content.get(0).getProperty().getAddress()).isEqualTo("Calle A");
        assertThat(content.get(0).getAgency().getUsername()).isEqualTo("alpha_inmobiliaria");
        // [1] "Calle A" / "beta_inmobiliaria"
        assertThat(content.get(1).getProperty().getAddress()).isEqualTo("Calle A");
        assertThat(content.get(1).getAgency().getUsername()).isEqualTo("beta_inmobiliaria");
        // [2] "Calle B"
        assertThat(content.get(2).getProperty().getAddress()).isEqualTo("Calle B");
    }

    @Test
    void pagination_totalElementsAndPagesAreCorrect() {
        for (int i = 1; i <= 5; i++) {
            Property p = em.persist(prop("Pag " + i, "CABA", "CABA", 3, 100_000.0, true));
            em.persist(listing(agencyAlpha, p, 100_000.0, false));
        }
        em.flush();

        Page<AgencyProperty> page = repository.findAll(noFilters(), PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }

    private Page<AgencyProperty> search(Specification<AgencyProperty> spec) {
        return repository.findAll(spec, PageRequest.of(0, 50));
    }

    /** Sin filtros → devuelve todo lo activo y no eliminado */
    private Specification<AgencyProperty> noFilters() {
        return spec(null, null, null, null, null, null, null, null);
    }

    private Specification<AgencyProperty> spec(
            String city, String province, PropertyType type,
            Integer roomsMin, Integer roomsMax,
            Double priceMin, Double priceMax, String keyword) {
        return AgencyPropertySpecification.searchActiveListings(
                city, province, type, roomsMin, roomsMax, priceMin, priceMax, keyword);
    }

    private User user(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("pass");
        u.setProfileType(ProfileType.ADMIN);
        return u;
    }

    private Agency agency(String username, User admin) {
        Agency a = new Agency();
        a.setUsername(username);
        a.setEmail(username + "@test.com");
        a.setPassword("pass");
        a.setAdminUser(admin);
        return a;
    }

    /** Property con tipo HOUSE por defecto */
    private Property prop(String address, String city, String province,
                           int rooms, double price, boolean available) {
        return typedProp(PropertyType.HOUSE, address, city, province, rooms, price, available);
    }

    private Property typedProp(PropertyType type, String address, String city, String province,
                                int rooms, double price, boolean available) {
        Property p = new Property();
        p.setPropertyType(type);
        p.setAddress(address);
        p.setCity(city);
        p.setProvince(province);
        p.setRooms(rooms);
        p.setAreaSq(80.0);
        p.setPrice(price);
        p.setAvailable(available);
        p.setDescription("descripción de prueba");
        p.setCircumscription("1");
        p.setSection("A");
        p.setBlock("1");
        p.setParcel("1");
        return p;
    }

    private AgencyProperty listing(Agency agency, Property property, double price, boolean deleted) {
        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(agency);
        ap.setProperty(property);
        ap.setListedPrice(price);
        ap.setListedDate(LocalDate.now());
        ap.setDeleted(deleted);
        return ap;
    }
}

