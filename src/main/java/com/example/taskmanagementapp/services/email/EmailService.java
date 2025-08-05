package com.example.taskmanagementapp.services.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    private static final long DELAY_MILLIS = 500;
    private final BlockingQueue<EmailRequest> emailQueue = new LinkedBlockingQueue<>();

    @Value("${resend.api.key}")
    private String resendApiKey;
    @Value("${mail}")
    private String senderEmail;

    private Thread emailWorkerThread;
    private Resend resendClient;

    public record EmailRequest(String toEmail, String subject, String body) {}

    public void queueEmail(String toEmail, String subject, String body) {
        try {
            emailQueue.put(new EmailRequest(toEmail, subject, body));
            logger.info("Email queued for: {}", toEmail);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Failed to queue email for {}: {}", toEmail, e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        this.resendClient = new Resend(resendApiKey);
        emailWorkerThread = new Thread(new EmailWorker(), "EmailWorkerThread");
        emailWorkerThread.start();
        logger.info("EmailWorkerThread started.");
    }

    @PreDestroy
    public void destroy() {
        if (emailWorkerThread != null) {
            emailWorkerThread.interrupt();
            try {
                emailWorkerThread.join(5000);
                logger.info("EmailWorkerThread stopped gracefully.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("EmailWorkerThread did not stop gracefully.");
            }
        }
    }

    private void sendEmailDirectly(EmailRequest request) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(senderEmail)
                .to(request.toEmail())
                .subject(request.subject())
                .text(request.body())
                .build();

        try {
            CreateEmailResponse data = resendClient.emails().send(params);
            logger.info("Successfully sent email to {} with Resend ID: {}",
                    request.toEmail(), data.getId());
        } catch (ResendException e) {
            logger.error("Failed to send email to {}: {}",
                    request.toEmail(), e.getMessage());
        }
    }

    private class EmailWorker implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EmailRequest request = emailQueue.take();
                    sendEmailDirectly(request);
                    TimeUnit.MILLISECONDS.sleep(DELAY_MILLIS);
                } catch (InterruptedException e) {
                    logger.info("EmailWorkerThread interrupted, shutting down.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error in EmailWorkerThread: {}", e.getMessage(), e);
                }
            }
        }
    }
}
