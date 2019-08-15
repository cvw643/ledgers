package de.adorsys.ledgers.rest.posting.api;

import de.adorsys.ledgers.rest.annotation.PostingResource;
import io.swagger.annotations.Api;

@PostingResource
@Api(tags = "LDG010 - Mock-PostingData-Upload")
public interface PostingApi {
    String BASE_PATH = "/posting";
}
