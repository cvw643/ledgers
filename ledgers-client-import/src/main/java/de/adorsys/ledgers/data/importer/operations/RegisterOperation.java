package de.adorsys.ledgers.data.importer.operations;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;

class RegisterOperation implements ImportOperation {
	
	
	private final UserTO userTO;
	private final UserRoleTO role;
	private final UserMgmtRestClient usrMgmt;
	
	
	public RegisterOperation(UserTO user, UserRoleTO role, UserMgmtRestClient userMgmtRestClient) {
		this.userTO = user;
		this.role = role;
		this.usrMgmt = userMgmtRestClient;
	}
	
	@Override
	public OperationResult process(OperationResult prevResult) {
		UserTO registerUser = registerUser(usrMgmt);
		return operationResultFrom(registerUser);
	}

	private UserTO registerUser(UserMgmtRestClient usrMgmt) {
		ResponseEntity<UserTO> resp = usrMgmt.register(this.userTO.getLogin(), this.userTO.getEmail(),this.userTO.getPin(), this.role);
		return resp.getBody();
	}


	private OperationResult operationResultFrom(UserTO registerUser) {
		return  new ObjectMapper().convertValue(registerUser, OperationResult.class);
	}
	
}
