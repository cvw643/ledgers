package de.adorsys.ledgers.mockbank.simple.service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DepositAccountService {
    private final AccountRestClient ledgersAccount;
    private final UserAccountService userAccountService;
    private final UserContextService contextService;

    @Autowired
    public DepositAccountService(AccountRestClient ledgersAccount, UserAccountService userAccountService, UserContextService contextService) {
        this.ledgersAccount = ledgersAccount;
        this.userAccountService = userAccountService;
        this.contextService = contextService;
    }


    public void createDepositAccount(UserTO userTO, AccountDetailsTO accountDetailsTO) throws IOException {
        UserContext userContenxt = contextService.byLoginOrEx(userTO.getLogin());
        List<AccountDetailsTO> accessibleAccounts = accessibleAccounts(userContenxt);
        for (AccountDetailsTO myAccount : accessibleAccounts) {
            if (StringUtils.equals(myAccount.getIban(), accountDetailsTO.getIban())) {
                return;
            }
        }
        try {
            contextService.setContext(userContenxt);
            ResponseEntity<Void> res;
            HttpStatus statusCode;
            try {
                res = ledgersAccount.createDepositAccount(accountDetailsTO);
                statusCode = res.getStatusCode();
            } catch (FeignException f) {
                statusCode = HttpStatus.valueOf(f.status());
            }
            if (HttpStatus.OK.equals(statusCode) || HttpStatus.CONFLICT.equals(statusCode)) {
                // Reauthenticate.
                BearerTokenTO accessToken = userAccountService.authorize(userTO.getLogin(), userTO.getPin(), UserRoleTO.CUSTOMER);
                UserContext bag = contextService.bagByIbanOrEx(accountDetailsTO.getIban());
                bag.setAccessToken(accessToken);
            } else {
                throw new IOException(String.format("Error creating account responseCode %s message %s.",
                        statusCode.value(), statusCode));
            }
        } finally {
            contextService.unsetContext();
        }
    }

    public List<AccountDetailsTO> accessibleAccounts(UserContext userBag)
            throws IOException {
        try {
            contextService.setContext(userBag);
            return ledgersAccount.getListOfAccounts().getBody();
        } catch (FeignException f) {
            throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
                    f.status(), f.getMessage()));
        } finally {
            contextService.unsetContext();
        }
    }

    public Optional<AccountDetailsTO> account(String iban)
            throws IOException {
        try {
            contextService.setContextFromIban(iban);
            return Optional.ofNullable(ledgersAccount.getAccountDetailsByIban(iban).getBody());
        } catch (FeignException f) {
            HttpStatus statusCode = HttpStatus.valueOf(f.status());
            if (HttpStatus.NOT_FOUND.equals(statusCode)) {
                return Optional.empty();
            } else {
                throw new IOException(String.format("Error reading account details responseCode %s message %s.",
                        f.status(), f.getMessage()));
            }
        } finally {
            contextService.unsetContext();
        }
    }

    public IOException numberFormater(String iban) {
        return new IOException(String.format("Deposit account with iban %s not found.", iban));
    }
}
