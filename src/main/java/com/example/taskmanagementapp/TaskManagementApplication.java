package com.example.taskmanagementapp;

import static com.example.taskmanagementapp.constants.Constants.GREEN;
import static com.example.taskmanagementapp.constants.Constants.RESET;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TaskManagementApplication {
    private static final Logger logger = LogManager.getLogger(TaskManagementApplication.class);
    @Value("${dropbox.refresh.token}")
    private String refreshToken;
    @Value("${dropbox.key}")
    private String key;
    @Value("${dropbox.secret}")
    private String secret;

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }

    @Bean
    public DbxClientV2 dropboxClient() throws ForbiddenException {
        String thisEmail;
        DbxClientV2 client;
        try {
            DbxRequestConfig config =
                    DbxRequestConfig.newBuilder("dropbox/taskmanagementapp").build();

            DbxCredential credential = new DbxCredential(
                    "",
                    0L,
                    refreshToken,
                    key,
                    secret);
            client = new DbxClientV2(config, credential);
            thisEmail = client.users().getCurrentAccount().getEmail();
        } catch (DbxException e) {
            throw new ForbiddenException("Couldn't obtain connection to dropbox");
        }
        logger.info(GREEN + "Connection to dropbox account {} obtained successfully"
                + RESET, thisEmail);
        return client;
    }
}
