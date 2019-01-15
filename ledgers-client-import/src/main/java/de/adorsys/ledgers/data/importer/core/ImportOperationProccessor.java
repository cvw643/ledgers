package de.adorsys.ledgers.data.importer.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import de.adorsys.ledgers.data.importer.data.MockbankInitData;
import de.adorsys.ledgers.data.importer.operations.ImportOperation;
import de.adorsys.ledgers.data.importer.operations.OperationFactory;
import de.adorsys.ledgers.data.importer.operations.OperationResult;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

@Component
public class ImportOperationProccessor {
	
	private final OperationFactory factory;
	
	public ImportOperationProccessor(OperationFactory factory) {
		this.factory = factory;
	}

	public boolean processImport(final MockbankInitData data) {
		List<UserTO> users = data.getUsers();
		List<UserTO> validUsers = users.stream()
										.filter(user -> hasLoginPinEmail(user))
										.collect(Collectors.toList());
		validUsers.forEach(user -> {
			final String key = "operation_result";
			final Map<String,OperationResult> temp = new HashMap<String, OperationResult>(1);
			
			int numberOfPerations = 7;
			// Register import operation set.
			final Queue<ImportOperation> queue = new ArrayBlockingQueue<ImportOperation>(numberOfPerations);
			
			queue.add(factory.newRegistrationOp(user, UserRoleTO.CUSTOMER));
			queue.add(factory.newLoginOperation(UserRoleTO.CUSTOMER));
			queue.add(factory.newCreateBankAccountOperation(data.getAccounts()));
			queue.add(factory.newLogoutOperation());
			queue.add(factory.newLoginOperation(UserRoleTO.CUSTOMER)); // - Login again to refresh access token with newly created accounts
			queue.add(factory.newSinglePaymentOperation(data.getSinglePayments()));
			queue.add(factory.newBulkPaymentOperation(data.getBulkPayments()));
			
			queue.iterator().forEachRemaining(op -> {
				OperationResult opResult = op.process(temp.get(key)); // Pipe in last stored operation result
				temp.put(key, opResult); // Override stored result. 
			});
			// Operation Validation : Performs Data Consistency Validation
		});
		return true;
	}

	private boolean hasLoginPinEmail(UserTO user) {
		boolean result = StringUtils.isNotBlank(user.getLogin());
		result = result && StringUtils.isNotBlank(user.getPin());
		result = result && StringUtils.isNotBlank(user.getEmail());
		return result;
	}

}
