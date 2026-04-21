package ar.edu.unq.backend.agency;

import ar.edu.unq.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "agency")
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agency_id")
    private Integer agencyId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

}