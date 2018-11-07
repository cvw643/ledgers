package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.AccountAccessEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.util.Assert;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.um.db.domain.AccessTypeEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.test.UmRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("AccountAccessRepositoryIT-db-entries.xml")
@DatabaseTearDown(value={"db-delete-all.xml"}, type= DatabaseOperation.DELETE_ALL)
public class AccountAccessEntityRepositoryIT {

    @Autowired
    private AccountAccessRepository accountAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test_create_ok() {

        AccountAccessEntity accountAccess = new AccountAccessEntity();
        accountAccess.setId(Ids.id());
        accountAccess.setIban("FakeIban");
        accountAccess.setAccessTypeEntity(AccessTypeEntity.OWNER);
        UserEntity user = new UserEntity();
        user.setId(Ids.id());
        user.setPin("1234");
        user.setLogin("vne");
        user.setEmail("vne@adorsys.de");
        accountAccess.setUser(user);
        user.getAccountAccessEntities().add(accountAccess);
        accountAccess.setUser(user);
        userRepository.save(user);
        AccountAccessEntity result = accountAccessRepository.findById(accountAccess.getId()).orElse(null);
        Assert.notNull(result);

    }

}


