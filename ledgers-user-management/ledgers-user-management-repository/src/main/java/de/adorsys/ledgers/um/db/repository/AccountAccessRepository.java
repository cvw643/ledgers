package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.AccountAccessEntity;
import org.springframework.data.repository.CrudRepository;

public interface AccountAccessRepository extends CrudRepository<AccountAccessEntity, String> {


}
