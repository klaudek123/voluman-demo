package com.example.demo.Volunteer.Candidate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "candidate")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstname;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastname;

    @Column(name = "email", unique = true,nullable = false, length = 50)
    private String email;

    @Column(name = "phone", nullable = false, length = 19)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "street", nullable = false, length = 50)
    private String street;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "house_number", nullable = false, length = 10)
    private String houseNumber;

    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    @Column(name = "postal_number", nullable = false, length = 20)
    private String postalNumber;

    @Column(name = "sex", length = 1)
    private String sex;
}
