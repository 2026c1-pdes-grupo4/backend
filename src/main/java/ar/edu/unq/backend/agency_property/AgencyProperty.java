package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.property.Property;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "agency_property")
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

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
