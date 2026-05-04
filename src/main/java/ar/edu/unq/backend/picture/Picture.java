package ar.edu.unq.backend.picture;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "picture")
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "picture_id")
    private Integer pictureId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agency_property_id", nullable = false)
    private AgencyProperty agencyProperty;

    @Column(nullable = false)
    private String url;
}

