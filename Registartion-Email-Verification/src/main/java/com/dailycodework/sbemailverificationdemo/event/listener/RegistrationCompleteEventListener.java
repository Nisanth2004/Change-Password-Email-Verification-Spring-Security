package com.dailycodework.sbemailverificationdemo.event.listener;

import com.dailycodework.sbemailverificationdemo.event.RegistrationCompleteEvent;
import com.dailycodework.sbemailverificationdemo.user.User;
import com.dailycodework.sbemailverificationdemo.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
@Slf4j// to dispaly url in console
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent>
{
private final UserService userService;
private final JavaMailSender mailSender;
private User theUser;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event)
    {
        // 1.Get newly registeted user
         theUser=event.getUser();

        // 2.Create a verification Token for the user
        String verificationToken= UUID.randomUUID().toString();

        // 3.save the verification token for the user
       userService.saveUserVerificationToken(theUser,verificationToken);

        // 4.Build the verification url to be sent to the user
        String url=event.getApplicationUrl()+"/register/verifyEmail?token="+verificationToken;

        // 5.Sent the email

        try
        {
            sendVerificationEmail(url);
        }
        catch (MessagingException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        log.info("Click the link to verify your registration: {}",url);
    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException
    {
        String subject="Email Verification";
        String senderName="Jaysan Agri Industrial Login Portal";
        String mailContent = "<p> Hi, "+ theUser.getFirstname()+ ", </p>"+
                "<p>Thank you for registering with us,"+"" +
                "Please, follow the link below to complete your registration.</p>"+
                "<a href=\"" +url+ "\">Verify your email to activate your account</a>"+
                "<p> Thank you <br> Jaysan Agri Industrial Login Portal";
        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("nisanthselva2004@gmail.com", senderName);
        messageHelper.setTo(theUser.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }

    public void sendPasswordResetVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException
    {
        String subject="Password Reset Requset Verification";
        String senderName="Jaysan Agri Industrial Login Portal";
        String mailContent = "<p> Hi, "+ theUser.getFirstname()+ ", </p>"+
                "<p>You recently requested to reset your password,"+"" +
                "Please, follow the link below to complete your registration.</p>"+
                "<a href=\"" +url+ "\">Reset Password</a>"+
                "<p> Thank you <br> Jaysan Agri Industrial Login Portal";
        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("nisanthselva2004@gmail.com", senderName);
        messageHelper.setTo(theUser.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }
}
