package de.adorsys.ledgers.data.importer.operations;

public interface ImportOperation {
	OperationResult process(OperationResult prevResult);
}
