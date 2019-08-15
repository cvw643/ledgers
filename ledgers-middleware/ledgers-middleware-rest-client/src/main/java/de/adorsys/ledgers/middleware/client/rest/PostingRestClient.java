package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.postings.rest.api.controller.PostingRestApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "postingRest", url = LedgersURL.LEDGERS_URL, path = PostingRestApi.BASE_PATH)
public interface PostingRestClient extends PostingRestApi {
}
