package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.DECOUPLED;

@Service
@RequiredArgsConstructor
public class DecoupledSender implements SCASender {
    private final EmailSender emailSender;

    @Override
    public boolean send(String value, String authCode) {
        return emailSender.send(value, authCode);
    }

    @Override
    public ScaMethodTypeBO getType() {
        return DECOUPLED;
    }
}
