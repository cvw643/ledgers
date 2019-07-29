package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.core.ResetPassword;
import de.adorsys.ledgers.core.SendCode;
import de.adorsys.ledgers.core.UpdatePassword;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG007 - Reset password for user")
public interface ResetPasswordAPI {
    String BASE_PATH = "/password";

    @PostMapping
    @ApiOperation(value = "Send code for user password reset")
    ResponseEntity<SendCode> sendCode(@RequestBody ResetPassword resetPassword);

    @PutMapping
    @ApiOperation(value = "Update user password")
    ResponseEntity<UpdatePassword> updatePassword(@RequestBody ResetPassword resetPassword);
}
