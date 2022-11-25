package com.example.loginapi.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.example.loginapi.Exception.TokenRefreshException;
import com.example.loginapi.models.*;
import com.example.loginapi.payload.request.LoginRequest;
import com.example.loginapi.payload.request.PasswordRequest;
import com.example.loginapi.payload.request.SignupRequest;
import com.example.loginapi.payload.response.JwtResponse;
import com.example.loginapi.payload.response.MessageResponse;
import com.example.loginapi.repository.CargoRepository;
import com.example.loginapi.repository.EmailServiceImpl;
import com.example.loginapi.repository.UsuarioRepository;
import com.example.loginapi.repository.security.jwt.JwtUtils;
import com.example.loginapi.repository.security.services.RefreshTokenService;
import com.example.loginapi.repository.security.services.UserDetailsImpl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new JwtResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/signup")
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



    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principle.toString() != "anonymousUser") {
            Long userId = ((UserDetailsImpl) principle).getId();
            refreshTokenService.deleteByUserId(userId);
        }

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new MessageResponse("Deslogado!"));
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

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);

        if ((refreshToken != null) && (refreshToken.length() > 0)) {
            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshToken)
                                .body(new MessageResponse("Token is refreshed successfully!"));
                    })
                    .orElseThrow(() -> new TokenRefreshException(refreshToken,
                            "Refresh token is not in database!"));
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token is empty!"));
    }

    @RequestMapping("/getExchangeRateDetailsByCurrency")
    public @ResponseBody
    JsonObject getExchangeRateDetails(String  currency) throws IOException {

        JsonObject jsonObject = new JsonObject();
        String data = getExchangeRateData(currency);
        data = data.replaceAll("^\"|\"$", "");
        StringTokenizer jsonTokenizer = new StringTokenizer(data,",");
        String internalData[];
        String expectedExchangeRateOutput = null;
        ArrayList otherCurrencies = new ArrayList();

        // Here given provisions to get the
        // value of GBP and EUR for INR
        if (currency.equalsIgnoreCase("INR")) {
            otherCurrencies.add("GBP");
            otherCurrencies.add("EUR");
        }

        // Here given provisions to get the
        // value of INR and EUR for GBP
        if (currency.equalsIgnoreCase("GBP")) {
            otherCurrencies.add("INR");
            otherCurrencies.add("EUR");
        }

        // Here given provisions to get the value
        // of GBP and INR for EUR
        if (currency.equalsIgnoreCase("EUR")) {
            otherCurrencies.add("INR");
            otherCurrencies.add("GBP");
        }
        while (jsonTokenizer.hasMoreTokens()) {
            expectedExchangeRateOutput = jsonTokenizer.nextToken();
            internalData = StringUtils.split(expectedExchangeRateOutput,":");
            System.out.println(internalData[0]+internalData[1]);
            if (internalData[0].substring(2,internalData[0].length()-1).equalsIgnoreCase(currency)) {
                jsonObject.addProperty(currency, internalData[1]);
            }
            if (internalData[0].substring(1,internalData[0].length()-1).equalsIgnoreCase(otherCurrencies.get(0).toString())) {
                jsonObject.addProperty(otherCurrencies.get(0).toString(), internalData[1]);
            }
            if (internalData[0].substring(1,internalData[0].length()-1).equalsIgnoreCase(otherCurrencies.get(1).toString())) {
                jsonObject.addProperty(otherCurrencies.get(1).toString(), internalData[1]);
            }
        }

        return jsonObject;
    }

    private String getExchangeRateData(String currency) throws IOException {
        String data;
        StringBuilder responseData = new StringBuilder();
        JsonObject jsonObject = null;
        URL url = null;
        url = new URL("https://api.exchangerate-api.com/v4/latest/" + currency);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        // System.out.println("Response Code : " + responseCode);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                responseData.append(line);
            }
            jsonObject = new Gson().fromJson(responseData.toString(), JsonObject.class);

            data = jsonObject.get("rates").toString();

        }
        // System.out.println(data);
        return data;
    }

}
