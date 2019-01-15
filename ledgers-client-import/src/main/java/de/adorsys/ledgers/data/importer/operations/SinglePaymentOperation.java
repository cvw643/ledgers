package de.adorsys.ledgers.data.importer.operations;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.ledgers.data.importer.data.SinglePaymentsData;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;

class SinglePaymentOperation implements ImportOperation{
	
	
	private final List<SinglePaymentsData> singlePayments;
	private final PaymentRestClient paymentRestClient;
	
	public SinglePaymentOperation(List<SinglePaymentsData> singlePayments, PaymentRestClient paymentRestClient) {
		super();
		this.singlePayments = singlePayments;
		this.paymentRestClient = paymentRestClient;
	}

	@Override
	public OperationResult process(OperationResult prevResult) {
		Objects.requireNonNull(prevResult);
		String bearerToken = (String) prevResult.get("accessToken");
		assertValidOrException(bearerToken);
		// Post all bank accounts
		
		singlePayments.forEach(pymt -> {
			SCAPaymentResponseTO resp = paymentRestClient.initiatePayment(PaymentTypeTO.SINGLE, pymt).getBody();
			// Authorize
		});
		OperationResult result = new OperationResult();
		result.put("accessToken", prevResult.get("accessToken"));
		return result;
	}
	private void assertValidOrException(String bearerToken) {
		if(StringUtils.isBlank(bearerToken)) throw new IllegalArgumentException("Bearer token is required for creating singlepayments");
	}


}
