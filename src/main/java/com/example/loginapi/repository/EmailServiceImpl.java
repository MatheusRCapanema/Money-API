package com.example.loginapi.repository;

import com.example.loginapi.models.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailRepository{

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;


    @Override
    public String enviarEmail(EmailDetails details) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();



            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getDestinatario());
            mailMessage.setText("https://www.google.com/");
            mailMessage.setSubject("Alterando Senha");

            javaMailSender.send(mailMessage);
            return "Enviado com sucesso";
        } catch( Exception e){
            return "Falha no envio";
        }

    }
}
