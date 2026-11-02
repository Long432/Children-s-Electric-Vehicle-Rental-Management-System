package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageRepository extends JpaRepository<Package, Long> {
    /** Các gói giá đang áp dụng, ít phút nhất trước. */
    List<Package> findByActiveTrueOrderByMinutesAsc();
}
