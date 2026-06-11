package com.tcc.price_finder_api.controller.ebay;

import com.tcc.price_finder_api.service.ebay.EbayAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ebay/auth")
@RequiredArgsConstructor
public class EbayAuthController {

    private final EbayAuthService authService;

    @GetMapping("/token")
    public Mono<String> getToken() {
        return authService.getValidToken();
    }
}
