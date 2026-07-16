package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.property.PropertyType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para la búsqueda dinámica de publicaciones activas.
 */
public class AgencyPropertySpecification {

    private AgencyPropertySpecification() {}

    public static Specification<AgencyProperty> searchActiveListings(
            String city,
            String province,
            PropertyType propertyType,
            Integer roomsMin,
            Integer roomsMax,
            Double priceMin,
            Double priceMax,
            String keyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtros base obligatorios
            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.isTrue(root.get("property").get("available")));

            // Filtros opcionales
            if (city != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("property").get("city")),
                        "%" + city.toLowerCase() + "%"));
            }
            if (province != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("property").get("province")),
                        "%" + province.toLowerCase() + "%"));
            }
            if (propertyType != null) {
                predicates.add(cb.equal(root.get("property").get("propertyType"), propertyType));
            }
            if (roomsMin != null) {
                predicates.add(cb.ge(root.get("property").get("rooms"), roomsMin));
            }
            if (roomsMax != null) {
                predicates.add(cb.le(root.get("property").get("rooms"), roomsMax));
            }
            if (priceMin != null) {
                predicates.add(cb.ge(root.get("listedPrice"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(cb.le(root.get("listedPrice"), priceMax));
            }
            if (keyword != null) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("property").get("address")), pattern),
                        cb.like(cb.lower(root.get("property").get("description")), pattern),
                        cb.like(cb.lower(root.get("property").get("city")), pattern),
                        cb.like(cb.lower(root.get("property").get("province")), pattern)
                ));
            }

            // Ordenamiento: solo para queries de datos (no para el count de paginación)
            if (!Long.class.equals(query.getResultType())) {
                query.orderBy(
                        cb.asc(root.get("property").get("address")),
                        cb.asc(root.get("agency").get("username"))
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

