package com.example.demo.Volunteer.User;


import com.example.demo.Volunteer.Volunteer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "user_")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDate dateOfChangePassword;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "volunteer_id", referencedColumnName = "volunteerId")
    @JsonIgnore
    private Volunteer volunteer = new Volunteer();
}

