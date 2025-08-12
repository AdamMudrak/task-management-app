package com.example.taskmanagementapp.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.ActionToken;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActionTokenRepositoryTest {
    private static final String ACTION_TOKEN = UUID.randomUUID().toString();
    private static final String NOT_EXISTING_ACTION_TOKEN = UUID.randomUUID().toString();
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private ActionTokenRepository actionTokenRepository;
    
    @BeforeEach
    void setUp() {
        ActionToken actionToken = ActionToken.builder().actionToken(ACTION_TOKEN).build();
        actionTokenRepository.save(actionToken);
    }

    @Test
    void givenActionToken_whenExistsByActionToken_thenReturnTrue() {
        assertTrue(actionTokenRepository.existsByActionToken(ACTION_TOKEN));
    }

    @Test
    void givenDeletedActionToken_whenExistsByActionToken_thenReturnFalse() {
        assertTrue(actionTokenRepository.existsByActionToken(ACTION_TOKEN));
        actionTokenRepository.deleteByActionToken(ACTION_TOKEN);
        assertFalse(actionTokenRepository.existsByActionToken(ACTION_TOKEN));
    }

    @Test
    void givenNotExistingActionToken_whenExistsByActionToken_thenReturnFalse() {
        assertFalse(
                actionTokenRepository.existsByActionToken(NOT_EXISTING_ACTION_TOKEN));
    }
}
