package ar.edu.unq.backend.favorite;

import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "favorite", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "property_id"})})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Integer favoriteId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "saved_date")
    private LocalDate savedDate;

    @Column(name = "saved_price")
    private Double savedPrice;

    private Integer score;

    @Column
    private String comment;
}