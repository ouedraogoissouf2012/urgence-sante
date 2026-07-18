package com.urgencesante.orientation.internal.application.port.in;

import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import com.urgencesante.orientation.internal.domain.model.Recommendation;
import java.util.List;

/** Port entrant : recommander des établissements pour un besoin médical. */
public interface RecommendFacilitiesUseCase {

    List<Recommendation> recommend(OrientationQuery query);
}
