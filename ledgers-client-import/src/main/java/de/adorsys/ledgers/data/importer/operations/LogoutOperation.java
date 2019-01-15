package de.adorsys.ledgers.data.importer.operations;

class LogoutOperation implements ImportOperation {

	@Override
	public OperationResult process(OperationResult prevResult) {
		return new OperationResult(); // Clear the context
	}

}
