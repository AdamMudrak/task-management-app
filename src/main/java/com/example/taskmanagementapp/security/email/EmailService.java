package com.example.taskmanagementapp.security.email;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_GRID_API_KEY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SUPPORT_EMAIL;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value(SUPPORT_EMAIL)
    protected String supportEmail;

    @Value(SEND_GRID_API_KEY)
    private String sendGridApiKey;

    public void sendMessage(String toEmail, String subject, String body) {
        Email from = new Email(supportEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
