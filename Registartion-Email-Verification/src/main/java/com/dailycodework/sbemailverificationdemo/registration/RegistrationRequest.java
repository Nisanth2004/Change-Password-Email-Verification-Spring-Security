package com.dailycodework.sbemailverificationdemo.registration;
import org.hibernate.annotations.NaturalId;
public record RegistrationRequest(
        String firstname,
        String lastname,
        String email,
        String password,
        String role) {
}