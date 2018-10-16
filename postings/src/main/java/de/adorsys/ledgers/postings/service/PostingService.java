package de.adorsys.ledgers.postings.service;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;

public interface PostingService {

    /**
     * Creates a new Posting.
     *
     * - If there is another posting with the same operation id
     * - The new posting can only be stored is the oldest is not part of a closed accounting period.
     * - A posting time can not be older than a closed accounting period.
     *
     * @param posting posting object to be persisted
     * @return Posting
     * @throws NotFoundException if posting can not be persisted the exception is thrown
     */
    Posting newPosting(Posting posting) throws NotFoundException;

    /**
     * Listing all postings associated with this operation id.
     *
     * @param oprId operation ID associated with posting(s)
     * @return Posting
     */
    List<Posting> findPostingsByOperationId(String oprId);

    /**
     * Compute the balance of a ledger account.
     *
     * @param ledgerAccount : the ledger account for which the balance shall be computed.
     * @param refTime       the time at which this balance has to be computed.
     * @return Posting
     * @throws NotFoundException If balance can not be computed, exception is thrown
     */
    Posting balanceTx(LedgerAccount ledgerAccount, LocalDateTime refTime) throws NotFoundException;
}
