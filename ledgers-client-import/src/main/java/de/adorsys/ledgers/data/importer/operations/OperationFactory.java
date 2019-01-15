package de.adorsys.ledgers.data.importer.operations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.adorsys.ledgers.data.importer.data.BulkPaymentsData;
import de.adorsys.ledgers.data.importer.data.SinglePaymentsData;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;

@Component
public class OperationFactory {

	private final AccountRestClient accountRestClient;
	private final UserMgmtRestClient userMgmtRestClient;
	private final PaymentRestClient paymentRestClient;
	
	@Autowired
	public OperationFactory(AccountRestClient accountRestClient, UserMgmtRestClient userMgmtRestClient,
			PaymentRestClient paymentRestClient) {
		super();
		this.accountRestClient = accountRestClient;
		this.userMgmtRestClient = userMgmtRestClient;
		this.paymentRestClient = paymentRestClient;
	}


	public ImportOperation newRegistrationOp(UserTO user, UserRoleTO role) {
		return new RegisterOperation(user, role, userMgmtRestClient);
	}


	public ImportOperation newLoginOperation(UserRoleTO role) {
		return new LoginOperation(role, userMgmtRestClient);
	}


	public ImportOperation newCreateBankAccountOperation(List<AccountDetailsTO> accounts) {
		return new CreateBankAccountOperation(accounts, accountRestClient);
	}


	public ImportOperation newLogoutOperation() {
		return new LogoutOperation();
	}


	public ImportOperation newSinglePaymentOperation(List<SinglePaymentsData> singlePayments) {
		return new SinglePaymentOperation(singlePayments, paymentRestClient);
	}


	public ImportOperation newBulkPaymentOperation(List<BulkPaymentsData> bulkPayments) {
		return new BulkPaymentOperation(bulkPayments, paymentRestClient);
	}
	
	
}
