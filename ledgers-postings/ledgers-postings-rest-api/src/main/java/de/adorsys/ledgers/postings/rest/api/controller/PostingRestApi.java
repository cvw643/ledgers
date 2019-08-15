package de.adorsys.ledgers.postings.rest.api.controller;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = "LDG009 - Mock-PostingData-Upload")
public interface PostingRestApi {
    String BASE_PATH = "/posting";

    @PostMapping(path = "/postings")
    @ApiOperation(value = "Creates new Postings.",
            notes = "- If there is another posting with the same operation id\n" +
                            "- The new posting can only be stored is the oldest is not part of a closed accounting period.\n" +
                            "- A posting time can not be older than a closed accounting period. ", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<List<PostingBO>> newPosting(List<PostingBO> postings);

    @GetMapping(path = "postings", params = {"oprId"})
    @ApiOperation(value = "Listing all postings associated with this operation id.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<List<PostingBO>> findPostingsByOperationId(@RequestParam(name = "oprId") String oprId);

}
