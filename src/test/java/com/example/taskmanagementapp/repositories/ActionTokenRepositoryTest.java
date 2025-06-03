package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.EntityFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ActionTokenRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private ActionTokenRepository actionTokenRepository;

    @BeforeEach
    public void setUp() {
        actionTokenRepository.save(EntityFactory.getActionToken());
    }

    @Test
    void givenActionToken_whenExistsByActionToken_thenReturnTrue() {
        Assertions.assertTrue(actionTokenRepository.existsByActionToken(Constants.ACTION_TOKEN));
    }

    @Test
    void givenDeletedActionToken_whenExistsByActionToken_thenReturnFalse() {
        Assertions.assertTrue(actionTokenRepository.existsByActionToken(Constants.ACTION_TOKEN));
        actionTokenRepository.deleteByActionToken(Constants.ACTION_TOKEN);
        Assertions.assertFalse(actionTokenRepository.existsByActionToken(Constants.ACTION_TOKEN));
    }

    @Test
    void givenNotExistingActionToken_whenExistsByActionToken_thenReturnFalse() {
        Assertions.assertFalse(
                actionTokenRepository.existsByActionToken(Constants.NOT_EXISTING_ACTION_TOKEN));
    }
}
