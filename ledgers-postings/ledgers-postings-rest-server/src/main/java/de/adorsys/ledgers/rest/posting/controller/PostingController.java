package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.rest.annotation.PostingResource;
import de.adorsys.ledgers.rest.posting.api.PostingApi;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PostingResource
@RequiredArgsConstructor
@RequestMapping(PostingApi.BASE_PATH)
public class PostingController implements PostingApi {
    private final PostingService postingService;

    /**
     * @param posting posting to create
     * @return persisted posting
     */
    @ApiOperation(value = "Creates a new Posting.",
            notes = "- If there is another posting with the same operation id\n" +
                            "- The new posting can only be stored is the oldest is not part of a closed accounting period.\n" +
                            "- A posting time can not be older than a closed accounting period. ")
    @PostMapping(path = "/postings")
    public ResponseEntity<PostingBO> newPosting(PostingBO posting) {
        PostingBO newPosting = postingService.newPosting(posting);
        return ResponseEntity.ok(newPosting);
    }

    /**
     * @param oprId operation identifier
     * @return a list of postings
     */
    @ApiOperation(value = "Listing all postings associated with this operation id.")
    @GetMapping(path = "postings", params = {"oprId"})
    public ResponseEntity<List<PostingBO>> findPostingsByOperationId(@RequestParam(name = "oprId") String oprId) {
        List<PostingBO> list = postingService.findPostingsByOperationId(oprId);
        return ResponseEntity.ok(list);
    }
}
