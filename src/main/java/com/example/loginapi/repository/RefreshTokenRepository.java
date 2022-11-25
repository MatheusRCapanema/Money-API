package com.example.loginapi.repository;

import com.example.loginapi.models.RefreshToken;
import com.example.loginapi.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(Usuario user);
}
