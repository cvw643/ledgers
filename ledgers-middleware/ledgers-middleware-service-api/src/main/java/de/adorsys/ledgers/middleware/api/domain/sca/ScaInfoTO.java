package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaInfoTO {
    private String userId;
    private String scaId;
    private String authorisationId;
    private UserRoleTO userRole;
    private String scaMethodId;
    private String authCode;
}
