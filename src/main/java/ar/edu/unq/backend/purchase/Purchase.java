package ar.edu.unq.backend.purchase;

import ar.edu.unq.backend.listing.AgencyProperty;
import ar.edu.unq.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "purchase")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Integer purchaseId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agency_property_id")
    private AgencyProperty agencyProperty;

    @Column(name = "purchase_price")
    private Double purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
}
