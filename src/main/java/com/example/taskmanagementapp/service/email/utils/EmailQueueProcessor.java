package com.example.taskmanagementapp.service.email.utils;

import com.example.taskmanagementapp.service.email.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueProcessor {
    private static final Logger logger = LogManager.getLogger(EmailQueueProcessor.class);
    private static final long DELAY_FOR_EMAIL_SEND = 500;
    private static final long DELAY_FOR_THREAD_STOP = 5000;

    private final BlockingQueue<EmailService.EmailRequest> emailQueue = new LinkedBlockingQueue<>();
    private final EmailSender emailSender;
    private Thread workerThread;

    public EmailQueueProcessor(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void queueEmail(EmailService.EmailRequest emailRequest) {
        try {
            emailQueue.put(emailRequest);
            logger.info("Email queued: {}", emailRequest.toEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Email queue interrupted: {}", e.getMessage());
        }
    }

    @PostConstruct
    public void start() {
        workerThread = new Thread(this::processQueue, "EmailWorkerThread");
        workerThread.start();
        logger.info("EmailWorkerThread started");
    }

    @PreDestroy
    public void stop() {
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(DELAY_FOR_THREAD_STOP);
                logger.info("EmailWorkerThread stopped");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("EmailWorkerThread did not stop gracefully: {}", e.getMessage());
            }
        }
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EmailService.EmailRequest request = emailQueue.take();
                emailSender.send(request);
                TimeUnit.MILLISECONDS.sleep(DELAY_FOR_EMAIL_SEND);
            } catch (InterruptedException e) {
                logger.info("EmailWorkerThread interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Unexpected error: {}", e.getMessage());
            }
        }
    }
}
