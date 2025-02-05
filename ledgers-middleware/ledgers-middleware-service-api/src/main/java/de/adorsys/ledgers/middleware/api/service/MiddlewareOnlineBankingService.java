package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

/**
 * Interface used for the initialization of user interaction. Implementation of
 * this interface will generally not require user interaction.
 *
 * @author fpo
 */
public interface MiddlewareOnlineBankingService {

    /**
     * Registers a User.
     *
     * @param login the login of the user
     * @param email the email of the user
     * @param pin   the pin of this user
     * @param role  the initial role of the user.
     * @return : user
     */
    UserTO register(String login, String email, String pin, UserRoleTO role);

    /**
     * Performs user authorization.
     * <p>
     * The returned String will be a signed JWT containing user access rights and
     * sca data.
     *
     * @param login User login
     * @param pin   User PIN
     * @param role  The intended role.
     * @return a session id for success, false for failure or trows a
     */
    SCALoginResponseTO authorise(String login, String pin, UserRoleTO role);

    /**
     * Special login associated with a account information, a payment or a payment cancellation consent.
     *
     * @param login           the login of the customer
     * @param pin             the password of the customer
     * @param consentId       the consentId or paymentId
     * @param authorisationId the authorisationId
     * @param opType          the operation type
     * @return login response
     */
    SCALoginResponseTO authoriseForConsent(String login, String pin, String consentId, String authorisationId, OpTypeTO opType);

    /**
     * Special login associated with a account information, a payment or a payment cancellation consent.
     *
     * @param consentId       the consentId or paymentId
     * @param authorisationId the authorisationId
     * @param opType          the operation type
     * @return login response
     */
    SCALoginResponseTO authoriseForConsentWithToken(ScaInfoTO scaInfo, String consentId, String authorisationId, OpTypeTO opType);

    /**
     * Caller can be sure that returned user object contains a mirror of permissions
     * contained in the token. This is generally a subset of permissions really held
     * by the user. If during validation we notice that the user has less permission
     * for the listed account, the token will be discarded an no user object will be
     * returned.
     *
     * @param accessToken : the access token
     * @return the bearer token
     */
    BearerTokenTO validate(String accessToken);

    // ================= SCA =======================================//

    /**
     * <p>
     * After the PSU selects the SCA method, this is called to generate and send the login auth code.
     *
     * @param scaInfoTO       SCA information
     * @param userMessage     message to user
     * @param validitySeconds validity in secondn.
     * @return SCALoginResponseTO the response object.
     */
    SCALoginResponseTO generateLoginAuthCode(ScaInfoTO scaInfoTO, String userMessage, int validitySeconds);

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     *
     * @param scaInfoTO : SCA information
     * @return the login response.
     */
    SCALoginResponseTO authenticateForLogin(ScaInfoTO scaInfoTO);
}
