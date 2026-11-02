package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "price_packages")
public class Package extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "minutes", nullable = false)
    private Integer minutes;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
