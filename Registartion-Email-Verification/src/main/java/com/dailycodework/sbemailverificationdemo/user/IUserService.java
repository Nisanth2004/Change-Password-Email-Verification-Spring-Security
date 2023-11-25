package com.dailycodework.sbemailverificationdemo.user;

import com.dailycodework.sbemailverificationdemo.registration.RegistrationRequest;
import com.dailycodework.sbemailverificationdemo.registration.token.VerificationToken;

import java.util.List;
import java.util.Optional;

public interface IUserService
{
    List<User> getUsers();
    User registerUser(RegistrationRequest request);
    Optional<User> findByEmail(String email);

    void saveUserVerificationToken(User theUser,String verificationToken);

    String validateToken(String theToken);

    VerificationToken generteNewVerificationToken(String oldToken);

    void createPasswordResetTokenForUser(User user, String passwordToken);

    String validatePasswordResetToken(String passwordResetToken);

    User findUserByPasswordToken(String passwordResetToken);

    void changePassword(User user, String newPassword);
}
