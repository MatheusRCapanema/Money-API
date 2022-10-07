package com.example.loginapi.repository;

import com.example.loginapi.models.Cargo;
import com.example.loginapi.models.EnumCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo,Long> {

    Optional<Cargo> findByName(EnumCargo name);

}
