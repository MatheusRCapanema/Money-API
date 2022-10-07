package com.example.loginapi.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.loginapi.models.Cargo;
import com.example.loginapi.models.EmailDetails;
import com.example.loginapi.models.EnumCargo;
import com.example.loginapi.models.Usuario;
import com.example.loginapi.payload.request.LoginRequest;
import com.example.loginapi.payload.request.PasswordRequest;
import com.example.loginapi.payload.request.SignupRequest;
import com.example.loginapi.payload.response.JwtResponse;
import com.example.loginapi.payload.response.MessageResponse;
import com.example.loginapi.repository.CargoRepository;
import com.example.loginapi.repository.EmailServiceImpl;
import com.example.loginapi.repository.UsuarioRepository;
import com.example.loginapi.repository.security.jwt.JwtUtils;
import com.example.loginapi.repository.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CargoRepository cargoRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/Login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> cargos = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new JwtResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        cargos));
    }


    @PostMapping("/registro")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        if (usuarioRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Usuário já está em uso!"));
        }

        if (usuarioRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email já está em uso"));
        }

        Usuario usuario = new Usuario(signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        Set<String> strCargos = signupRequest.getCargo();
        Set<Cargo> cargos = new HashSet<>();

        if (strCargos == null) {
            Cargo userRole = cargoRepository.findByName(EnumCargo.CARGO_USUARIO)
                    .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));
            cargos.add(userRole);
        } else {
            strCargos.forEach(cargo -> {
                switch (cargo) {
                    case "admin":
                        Cargo adminRole = cargoRepository.findByName(EnumCargo.CARGO_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));
                        cargos.add(adminRole);

                        break;
                    case "mod":
                        Cargo modRole = cargoRepository.findByName(EnumCargo.CARGO_MODERADOR)
                                .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));
                        cargos.add(modRole);

                        break;
                    default:
                        Cargo userRole = cargoRepository.findByName(EnumCargo.CARGO_USUARIO)
                                .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));
                        cargos.add(userRole);
                }
            });
        }

        usuario.setCargos(cargos);
        usuarioRepository.save(usuario);


        return ResponseEntity.ok(new MessageResponse("Registro bem sucedido"));
    }


    @PostMapping("/Logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Deslogado"));
    }

    @Autowired
    private EmailServiceImpl emailService;

    @PostMapping("/esqueciSenha")
    public String enviarEmail(@RequestBody EmailDetails emailDetails) {
        String status = emailService.enviarEmail(emailDetails);
        return status;

    }


    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordRequest passwordRequest) {
        Usuario usuario = usuarioRepository.findByEmail(passwordRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));
        usuario.setPassword(encoder.encode(passwordRequest.getPassword()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso!"));


    }
}
