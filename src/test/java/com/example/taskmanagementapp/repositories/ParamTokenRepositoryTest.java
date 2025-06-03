package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.EntityFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParamTokenRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private ParamTokenRepository paramTokenRepository;

    @BeforeEach
    void setUp() {
        paramTokenRepository.save(EntityFactory.getParamToken());
    }

    @Test
    void givenParamToken_whenExistsByParameterAndActionToken_thenReturnTrue() {
        Assertions.assertTrue(paramTokenRepository
                .existsByParameterAndActionToken(
                        Constants.PARAMETER, Constants.ACTION_TOKEN));
    }

    @Test
    void givenDeletedParamToken_whenExistsByParameterAndActionToken_thenReturnFalse() {
        Assertions.assertTrue(paramTokenRepository
                .existsByParameterAndActionToken(
                        Constants.PARAMETER, Constants.ACTION_TOKEN));
        paramTokenRepository.deleteByParameterAndActionToken(
                Constants.PARAMETER, Constants.ACTION_TOKEN);
        Assertions.assertFalse(paramTokenRepository
                .existsByParameterAndActionToken(
                        Constants.PARAMETER, Constants.ACTION_TOKEN));
    }

    @Test
    void givenRandomParamToken_whenExistsByParameterAndActionToken_thenReturnFalse() {
        Assertions.assertFalse(paramTokenRepository
                .existsByParameterAndActionToken(
                        Constants.NOT_EXISTING_PARAMETER, Constants.NOT_EXISTING_ACTION_TOKEN));
    }
}
