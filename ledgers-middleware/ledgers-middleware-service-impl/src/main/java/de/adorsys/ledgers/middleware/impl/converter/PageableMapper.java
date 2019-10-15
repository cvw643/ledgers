package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.um.PageableTO;
import de.adorsys.ledgers.um.api.domain.PageableBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PageMapper.class)
public interface PageableMapper {
    PageableBO toPageableBO(PageableTO pageableTO);
}
