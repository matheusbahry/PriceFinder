package com.tcc.price_finder_api.repo.api;

import com.tcc.price_finder_api.model.api.MercadoLivreToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;
public interface MercadoLivreTokenRepository extends ReactiveCrudRepository<MercadoLivreToken, UUID> {


}
