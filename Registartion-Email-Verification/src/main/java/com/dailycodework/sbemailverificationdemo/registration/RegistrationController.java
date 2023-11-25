package com.dailycodework.sbemailverificationdemo.registration;

import com.dailycodework.sbemailverificationdemo.event.RegistrationCompleteEvent;
import com.dailycodework.sbemailverificationdemo.event.listener.RegistrationCompleteEventListener;
import com.dailycodework.sbemailverificationdemo.registration.password.PasswordRequestUtil;
import com.dailycodework.sbemailverificationdemo.registration.token.VerificationToken;
import com.dailycodework.sbemailverificationdemo.registration.token.VerificationTokenRepository;
import com.dailycodework.sbemailverificationdemo.user.User;
import com.dailycodework.sbemailverificationdemo.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController
{
    private final ApplicationEventPublisher publisher;
    private final UserService userService;

    private final VerificationTokenRepository tokenRepository;

    private final RegistrationCompleteEventListener eventListener;
    private final HttpServletRequest servletRequest;

    @PostMapping
     public String registerUser(@RequestBody RegistrationRequest registrationRequest, final HttpServletRequest request)
  {
User user=userService.registerUser(registrationRequest);

    publisher.publishEvent(new RegistrationCompleteEvent(user,applicationUrl(request)));
    return "Success! Please,Check your email for to complete your registration";
}

@GetMapping("/verifyEmail")
public String verifyEmail(@RequestParam("token") String token)
{
    String url=applicationUrl(servletRequest)+"/register/resend-verification-token?token="+token;
    VerificationToken theToken=tokenRepository.findByToken(token);
    if (theToken.getUser().isEnabled())
    {
        return "This account has already been verified, please, login.";
    }
    String verificationResult = userService.validateToken(token);

    if (verificationResult.equalsIgnoreCase("valid"))
    {
        return "Email verified successfully. Now you can login to your account";
    }
    return "Invalid verification token, <a href=\"" +url+ "\"> Get a New Verification Link. </a>";
}

// Resend Verification Code

    @GetMapping("/resend-verification-token")
    public String resendVericiationToken(@RequestParam("token") String oldToken,
                                         HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
       VerificationToken verificationToken=userService.generteNewVerificationToken(oldToken);
       User theUser=verificationToken.getUser();
       resendVerificationTokenEmail(theUser,applicationUrl(request),verificationToken);
       return "A new verification link has been sent to your email,"
        + "please check to activate yur registration";
    }

    private void resendVerificationTokenEmail(User theUser,
                                             String applicationUrl,
                                             VerificationToken verificationToken) throws MessagingException, UnsupportedEncodingException {
        String url=applicationUrl+"/register/verifyEmail?token="+verificationToken.getToken();
        eventListener.sendVerificationEmail(url);
        log.info("Click the link to verify your registration: {}",url);

    }

    // forgot password will be come to this method and find the user then it
    // will create passwordResetUrl and it will go to next method(passwordResetEmailLink)
    @PostMapping("/password-reset-request")
    public String resetPasswordRequest(@RequestBody PasswordRequestUtil passwordResetRequest,
                                      final HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        // find the user
        Optional<User> user=userService.findByEmail(passwordResetRequest.getEmail());
        String passwordResetUrl="";
        if(user.isPresent())
        {
            String passwordResetToken= UUID.randomUUID().toString();
            // save a passwordResetToken in verification_token table
            userService.createPasswordResetTokenForUser(user.get(),passwordResetToken);
            passwordResetUrl=passwordResetEmailLink(user.get(),applicationUrl(request),passwordResetToken);
        }
        return passwordResetUrl;
    }

    // this end point will store the password in database(reset-password)
    private String passwordResetEmailLink(User user, String applicationUrl, String passwordResetToken)
            throws MessagingException, UnsupportedEncodingException
    {
        String url=applicationUrl+"/register/reset-password?token="+passwordResetToken;
        eventListener.sendPasswordResetVerificationEmail(url);
        log.info("Click the link to Reset Your Password: {}",url);
        return url;

    }

    // This is the endpoint
    // get New password from the User using @Requestparam
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody PasswordRequestUtil passwordResetRequest,
                                @RequestParam("token") String passwordResetToken)
    {
        // validate the token
        String tokenValidationResult=userService.validatePasswordResetToken(passwordResetToken);
        // if token is not valid
        if(!tokenValidationResult.equalsIgnoreCase("valid"))
        {
            return "InValid Password reset  Token";
        }
        // If the user is found set the password in db(resetUserPassword)
        User user=userService.findUserByPasswordToken(passwordResetToken);
        if(user!=null)
        {
            userService.changePassword(user,passwordResetRequest.getNewPassword());
            return "Password has been Reset Successfully";
        }

        return "Invalid Password reset token";
    }


    // Implement the Chane password functionality
    @PostMapping("/change-password")
    public String changePassword(@RequestBody PasswordRequestUtil requestUtil)
    {
        User user=userService.findByEmail(requestUtil.getEmail()).get();
        // Id oldpassword does not match
        if(!userService.oldPasswordIsValid(user,requestUtil.getOldPassword()))
        {
            return "Incorrect Old Password";
        }

        userService.changePassword(user,requestUtil.getNewPassword());
        return "Password changed successfully";
    }

    public String applicationUrl(HttpServletRequest request)
    {
    return "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
    }
}
