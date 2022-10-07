package com.example.loginapi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class AutorizandoController {

        @GetMapping("/all")
        public String allAccess() {
            return "Public Content.";
        }

        @GetMapping("/user")
        @PreAuthorize("hasRole('CARGO_USUARIO') or hasRole('CARGO_MODERADOR') or hasRole('CARGO_ADMIN')")
        public String userAccess() {
            return "USUARIO.";
        }

        @GetMapping("/mod")
        @PreAuthorize("hasRole('MODERADOR')")
        public String moderatorAccess() {
            return "MODERADOR.";
        }

        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminAccess() {
            return "ADMIN.";
        }
    }

