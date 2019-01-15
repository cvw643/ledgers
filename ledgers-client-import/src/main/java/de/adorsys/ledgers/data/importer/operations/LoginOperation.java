package de.adorsys.ledgers.data.importer.operations;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;

class LoginOperation implements ImportOperation {
	
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final UserMgmtRestClient usrMgmt;
	private final UserRoleTO role;
	
	
	public LoginOperation(UserRoleTO role, UserMgmtRestClient userMgmtRestClient) {
		this.usrMgmt = userMgmtRestClient;
		this.role = role;
	}

	@Override
	public OperationResult process(OperationResult prevResult) {
		validate(prevResult);
		String login = (String) prevResult.get("login");
		String pin = (String) prevResult.get("pin");
		
		SCALoginResponseTO resp = usrMgmt.authorise(login, pin, role).getBody();
		return operationResultFrom(resp);
	}

	private OperationResult operationResultFrom(SCALoginResponseTO resp) {
		return mapper.convertValue(resp, OperationResult.class);
	}
	private void validate(OperationResult prevResult) {
		Objects.requireNonNull(prevResult);
	}

}
