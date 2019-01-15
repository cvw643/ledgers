package de.adorsys.ledgers.data.importer.operations;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;

class CreateBankAccountOperation implements ImportOperation {

	private final List<AccountDetailsTO> accounts;
	private AccountRestClient accountRestClient;
	
	public CreateBankAccountOperation(List<AccountDetailsTO> accounts, AccountRestClient accountRestClient) {
		this.accounts = accounts;
		this.accountRestClient = accountRestClient;
	}

	@Override
	public OperationResult process(OperationResult prevResult) {
		Objects.requireNonNull(prevResult);
		String bearerToken = (String) prevResult.get("accessToken");
		assertValidOrException(bearerToken);

		// Post all bank accounts
		accounts.forEach(acc -> {
			accountRestClient.createDepositAccount(acc);
		});
		
		OperationResult operationResult = new OperationResult();
		operationResult.put("accessToken", prevResult.get("accessToken"));
		return operationResult;
	}

	private void assertValidOrException(String bearerToken) {
		if (StringUtils.isBlank(bearerToken))
			throw new IllegalArgumentException("Bearer token is required for creating bank accounts");
	}

}
