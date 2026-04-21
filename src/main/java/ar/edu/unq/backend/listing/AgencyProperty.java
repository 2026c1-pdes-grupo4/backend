package ar.edu.unq.backend.listing;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.property.Property;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "agency_property", uniqueConstraints = {@UniqueConstraint(columnNames = {"agency_id", "property_id"})})
public class AgencyProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_property_id")
    private Integer agencyPropertyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "listed_date")
    private LocalDate listedDate;

    @Column(name = "listed_price", nullable = false)
    private Double listedPrice;

    @Column(nullable = false)
    private Boolean active = true;
}

