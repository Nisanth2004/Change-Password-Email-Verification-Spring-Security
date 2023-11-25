package com.dailycodework.sbemailverificationdemo.user;

import com.dailycodework.sbemailverificationdemo.exception.UserAlreadyExistsException;
import com.dailycodework.sbemailverificationdemo.registration.RegistrationRequest;
import com.dailycodework.sbemailverificationdemo.registration.password.PasswordRequestUtil;
import com.dailycodework.sbemailverificationdemo.registration.password.PasswordResetTokenService;
import com.dailycodework.sbemailverificationdemo.registration.token.VerificationToken;
import com.dailycodework.sbemailverificationdemo.registration.token.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // autowired to be default only final will taken
public class UserService implements IUserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        Optional<User> user = this.findByEmail(request.email());
        // cheack user exists
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User with email" + request.email() + "alreday exists");
        }
        var newUser = new User();
        newUser.setFirstname(request.firstname());
        newUser.setLastname(request.lastname());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());
        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUserVerificationToken(User theUser, String token) {
        var verificationToken = new VerificationToken(token, theUser);
        tokenRepository.save(verificationToken);
    }

    @Override
    public String validateToken(String theToken)
    {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token == null)
        {
            return "Invalid verification token";
        }

        User user = token.getUser();
        Calendar calendar = Calendar.getInstance();

        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0)
        {
            tokenRepository.delete(token);
            return "Verification Link already Token  expired";
        }

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generteNewVerificationToken(String oldToken)
    {
        // getting a old token
        VerificationToken verificationToken=tokenRepository.findByToken(oldToken);
         var tokenExpirationTime=new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(tokenExpirationTime.getTokenExpirationTime());

        return tokenRepository.save(verificationToken);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String passwordToken)
    {
        passwordResetTokenService.createPasswordResetTokenForUser(user,passwordToken);
    }

    @Override
    public String validatePasswordResetToken(String passwordResetToken)
    {
        return passwordResetTokenService.validatePasswordResetToken(passwordResetToken);
    }

    @Override
    public User findUserByPasswordToken(String passwordResetToken)
    {
        return passwordResetTokenService.findUserByPasswordToken(passwordResetToken).get();
    }

    @Override
    public void changePassword(User user, String newPassword)
    {
        // Save the reset password in db
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    // Implement the Chane password functionality
    public boolean oldPasswordIsValid(User user,String oldPassword)
    {
        // check the old encrypted password using passwordEncoder
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
