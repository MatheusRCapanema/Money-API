package com.example.loginapi.repository.security.services;

import com.example.loginapi.models.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
    Se o processo de autenticação der certo nós podemos chaamr
    as informações do usuario chamando apenas o objeto de Autenticação:

        Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    // userDetails.getUsername()
    // userDetails.getPassword()
    // userDetails.getAuthorities()


 */

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password
            , Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }


    /*
    Perceba uma coisa, precisamos converter todos aqueles cargos que criamos
    em uma lista de Permissões para enquadrarmos no padrão se Segurança do
    Spring, para isso ultilizamos List<GrantedAuthority> que é uma stream
    de <Cargo>
     */


    public static UserDetailsImpl build(Usuario usuario){
        List<GrantedAuthority> authorities = usuario.getCargos().stream()
                .map(role-> new SimpleGrantedAuthority(role.getNome().name()))
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
