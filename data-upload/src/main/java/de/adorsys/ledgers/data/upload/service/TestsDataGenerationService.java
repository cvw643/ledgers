package de.adorsys.ledgers.data.upload.service;

import de.adorsys.ledgers.data.upload.model.AccountBalance;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.utils.IbanGenerator;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestsDataGenerationService {
    private final AccessTokenTO accessToken;
    private final UserService userService;
    private final ParseService parseService;
    private final RestExecutionService executionService;

    public TestsDataGenerationService(AccessTokenTO accessToken, UserService userService, ParseService parseService, RestExecutionService executionService) {
        this.accessToken = accessToken;
        this.userService = userService;
        this.parseService = parseService;
        this.executionService = executionService;
    }

    public byte[] generate(String token) throws UserNotFoundMiddlewareException, FileNotFoundException {
        try {
            String branch = userService.findByLogin(accessToken.getLogin()).getBranch();
            DataPayload dataPayload = parseService.getDefaultData()
                                              .map(d -> generateData(d, branch))
                                              .orElseThrow(() -> new FileNotFoundException("Seems no data is present in file!"));
            executionService.updateLedgers(token, dataPayload);
            return parseService.getFile(dataPayload);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundMiddlewareException(e);
        }
    }

    private DataPayload generateData(DataPayload data, String branch) {
        Map<String, AccountDetailsTO> detailsMap = Optional.ofNullable(data.getAccounts()).orElse(Collections.emptyList()).stream()
                                                           .map(a -> generateDetails(a, branch))
                                                           .collect(Collectors.toMap(a -> a.getIban().substring(a.getIban().length() - 2), a -> a));
        data.setAccounts(new ArrayList<>(detailsMap.values()));
        List<AccountBalance> balances = Optional.ofNullable(data.getBalancesList()).orElse(Collections.emptyList()).stream()
                                                .map(b -> generateBalances(b, branch, detailsMap))
                                                .collect(Collectors.toList());
        data.setBalancesList(balances);
        List<UserTO> users = Optional.ofNullable(data.getUsers()).orElse(Collections.emptyList()).stream()
                                     .map(u -> generateUsers(u, branch, detailsMap))
                                     .collect(Collectors.toList());
        data.setUsers(users);
        return data;
    }

    private AccountBalance generateBalances(AccountBalance balance, String branch, Map<String, AccountDetailsTO> detailsMap) {
        String iban = getGeneratedIbanOrNew(balance.getIban(), branch, detailsMap);
        balance.setIban(iban);
        return balance;
    }

    private AccountDetailsTO generateDetails(AccountDetailsTO details, String branch) {
        String iban = generateIban(branch, details.getIban());
        details.setIban(iban);
        return details;
    }

    private UserTO generateUsers(UserTO user, String branch, Map<String, AccountDetailsTO> detailsMap) {
        user.setId(branch + "_" + user.getId());
        user.setEmail(branch + "_" + user.getEmail());
        user.setLogin(branch + "_" + user.getLogin());
        user.getScaUserData()
                .forEach(d -> d.setMethodValue(branch + "_" + d.getMethodValue()));
        user.getAccountAccesses()
                .forEach(a -> a.setIban(getGeneratedIbanOrNew(a.getIban(), branch, detailsMap)));
        return user;
    }

    private String generateIban(String branch, String iban) {
        return IbanGenerator.generateIban(branch, iban);
    }

    private String getGeneratedIbanOrNew(String iban, String branch, Map<String, AccountDetailsTO> detailsMap) {
        return Optional.ofNullable(detailsMap.get(iban).getIban())
                       .orElseGet(() -> generateIban(branch, iban));
    }
}
