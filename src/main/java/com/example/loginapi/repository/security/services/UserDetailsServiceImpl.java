package com.example.loginapi.repository.security.services;

import com.example.loginapi.repository.UsuarioRepository;
import com.example.loginapi.models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
    Ultilizamos a UserDetailsService como uma forma de pegar as informações
    da UserDetails, graças a isso podemos olhar a service como um único método:

     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new
                        UsernameNotFoundException("Usuario com nome de usuario:"+ username));
        return UserDetailsImpl.build(usuario);
    }
}
