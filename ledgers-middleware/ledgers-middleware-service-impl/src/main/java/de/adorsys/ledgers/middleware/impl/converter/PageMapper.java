package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.um.PageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.PageBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PageMapper {
    PageTO<UserTO> toPageTO(PageBO<UserBO> pageBO);

    PageBO<UserBO> toPageBO(PageTO<UserTO> pageTO);
}
