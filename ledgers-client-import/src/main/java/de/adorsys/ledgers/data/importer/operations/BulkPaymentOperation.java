package de.adorsys.ledgers.data.importer.operations;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.ledgers.data.importer.data.BulkPaymentsData;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;

class BulkPaymentOperation implements ImportOperation{
	
	private final List<BulkPaymentsData> bulkPayments;
	private final PaymentRestClient paymentRestClient;
	private final AuthRequestInterceptor authRequestInterceptor;
	
	public BulkPaymentOperation(List<BulkPaymentsData> bulkPayments, PaymentRestClient paymentRestClient, AuthRequestInterceptor authRequestInterceptor) {
		super();
		this.bulkPayments = bulkPayments;
		this.paymentRestClient = paymentRestClient;
		this.authRequestInterceptor = authRequestInterceptor;
	}

	@Override
	public OperationResult process(OperationResult prevResult) {
		Objects.requireNonNull(prevResult);
		String bearerToken = (String) prevResult.get("accessToken");
		assertValidOrException(bearerToken);
		
		// Post all bank accounts
		bulkPayments.forEach(pymt -> {
			authRequestInterceptor.setAccessToken(bearerToken);
			paymentRestClient.initiatePayment(PaymentTypeTO.BULK, pymt);
			// Authorize
		});
		
		OperationResult result = new OperationResult();
		// Give back the access token. Maybe the next operation need it.
		result.put("accessToken", prevResult.get("accessToken"));
		return result;
	}
	private void assertValidOrException(String bearerToken) {
		if(StringUtils.isBlank(bearerToken)) {
			throw new IllegalArgumentException("Bearer token is required for creating bulkPayments");
		}
	}

}
