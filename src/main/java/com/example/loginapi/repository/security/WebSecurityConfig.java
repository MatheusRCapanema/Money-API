package com.example.loginapi.repository.security;

import com.example.loginapi.repository.security.jwt.AuthEntryPointJwt;
import com.example.loginapi.repository.security.jwt.AuthTokenFilter;
import com.example.loginapi.repository.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;




@Configuration

//Permite que o Spring ache e automaticamente aplique a classe uma configuração global do web security

//Provê aos metodos a segurança AOP (Aspect Oriented Programming)
//AOP aumenta a modularidade permiting a separação de conceitos cross-cutting
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {


    /*
    Spring Security vai carregar os detalhes dos usuarios para fazer uma
    autorização e autenticação, para isso precisamos implementar uma
    interface. A implementação dela vai ser usada para configurar
    a DaoAuthenticationProvider pelo metodo
    AuthenticationManagerBuilder.userDetailsService()
     */
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(){
        return new AuthTokenFilter();
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authProvider  = new DaoAuthenticationProvider();

        authProvider .setUserDetailsService(userDetailsService);
        authProvider .setPasswordEncoder(passwordEncoder());

        return authProvider ;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    /*
    Nós também precisamos de um passwordEncoder para o DaoAuthenticationProvider,
    se não fizermos isso ele vai usar só a String da senha
    */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*Sobrescrever este metodo com o WebSecurityConfigurerAdapter diz ao
    Spring como configurar o CORS e o CSRF para que quando fizermos uma
    requisição de todos os usuarios eles sejam autenticados ou não
    */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .anyRequest().authenticated();

        http.authenticationProvider(daoAuthenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
