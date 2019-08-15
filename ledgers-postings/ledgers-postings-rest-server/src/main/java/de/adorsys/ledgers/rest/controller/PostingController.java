package de.adorsys.ledgers.rest.controller;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.rest.api.annotation.PostingResource;
import de.adorsys.ledgers.postings.rest.api.controller.PostingRestApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@PostingResource
@RequiredArgsConstructor
@RequestMapping(PostingRestApi.BASE_PATH)
public class PostingController implements PostingRestApi {
    private final PostingService postingService;

    /**
     * @param postings list of postings to create
     * @return persisted posting
     */
    @Override
    public ResponseEntity<List<PostingBO>> newPosting(List<PostingBO> postings) {
        List<PostingBO> newPostings = postings.stream()
                                              .map(postingService::newPosting)
                                              .collect(Collectors.toList());
        return ResponseEntity.ok(newPostings);
    }

    /**
     * @param oprId operation identifier
     * @return a list of postings
     */
    @Override
    public ResponseEntity<List<PostingBO>> findPostingsByOperationId(String oprId) {
        List<PostingBO> list = postingService.findPostingsByOperationId(oprId);
        return ResponseEntity.ok(list);
    }
}
