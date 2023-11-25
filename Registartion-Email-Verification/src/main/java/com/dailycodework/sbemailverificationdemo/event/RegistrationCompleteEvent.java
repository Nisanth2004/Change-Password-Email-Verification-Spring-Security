package com.dailycodework.sbemailverificationdemo.event;

import com.dailycodework.sbemailverificationdemo.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private User user;// this user will publish event
    private String applicationUrl;

    public RegistrationCompleteEvent(User user,String applicationUrl)
    {
        super(user);
        this.applicationUrl=applicationUrl;
        this.user=user;
    }
}
