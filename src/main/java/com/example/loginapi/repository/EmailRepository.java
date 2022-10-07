package com.example.loginapi.repository;

import com.example.loginapi.models.EmailDetails;

public interface EmailRepository {
    String enviarEmail (EmailDetails details);

}
