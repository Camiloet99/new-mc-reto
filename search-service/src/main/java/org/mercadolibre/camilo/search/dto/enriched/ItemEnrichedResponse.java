package org.mercadolibre.camilo.search.dto.enriched;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewSummaryResponse;

import java.util.List;

@Value
@Builder
public class ItemEnrichedResponse {
    ItemBasicResponse basic;
    SellerResponse seller;
    ReviewSummaryResponse reviews;
    @Singular("qaItem")
    List<QaResponse> qa;
}
