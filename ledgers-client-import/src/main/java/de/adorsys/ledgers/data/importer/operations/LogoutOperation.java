package de.adorsys.ledgers.data.importer.operations;

import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;

class LogoutOperation implements ImportOperation {
	
	private final UserMgmtRestClient userMgmtRestClient;
	
	
	public LogoutOperation(UserMgmtRestClient userMgmtRestClient) {
		this.userMgmtRestClient = userMgmtRestClient;
	}

	@Override
	public OperationResult process(OperationResult prevResult) {
		// Maybe call the ledgers url for logout when available.
		return new OperationResult(); // Clear the context
	}

}
