package ar.edu.unq.backend.property;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    private String address;

    private String city;

    private String province;

    @Column(name = "area_sq")
    private Double areaSq;

    private Integer rooms;

    private String description;

    private String circumscription;

    private String section;

    private String block;

    private String parcel;

    private Boolean available;
}
