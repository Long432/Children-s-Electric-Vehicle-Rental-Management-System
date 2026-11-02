package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<org.group6.childrenelectricvehiclerentalmanagement.Models.User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByCookieHash(String cookieHash);
}
