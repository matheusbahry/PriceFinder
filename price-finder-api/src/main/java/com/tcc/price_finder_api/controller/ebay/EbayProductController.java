package com.tcc.price_finder_api.controller.ebay;

import com.tcc.price_finder_api.model.Product;
import com.tcc.price_finder_api.service.ebay.EbayProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ebay/products")
@RequiredArgsConstructor
public class EbayProductController {

    private final EbayProductService productService;

    // ===============================
    // 🔎 Buscar por palavra-chave
    // ===============================
    // GET /api/ebay/products/search?q=iphone

    @GetMapping("/search")
    public Flux<Product> searchByKeyword(
            @RequestParam String q
    ) {
        return productService.searchByKeyword(q);
    }

    // ===============================
    // 💰 Buscar por faixa de preço
    // ===============================
    // GET /api/ebay/products/price-range?q=iphone&min=100&max=500

    @GetMapping("/price-range")
    public Flux<Product> searchByPriceRange(
            @RequestParam String q,
            @RequestParam double min,
            @RequestParam double max
    ) {
        return productService.searchByPriceRange(q, min, max);
    }

    // ===============================
    // ⭐ Buscar ordenado
    // ===============================
    // GET /api/ebay/products/sorted?q=iphone&sort=price

    @GetMapping("/sorted")
    public Flux<Product> searchSorted(
            @RequestParam String q,
            @RequestParam(defaultValue = "bestMatch") String sort
    ) {
        return productService.searchSorted(q, sort);
    }

    // ===============================
    // 🆔 Buscar detalhes por ID
    // ===============================
    // GET /api/ebay/products/{id}

    @GetMapping("/{id}")
    public Mono<Product> getById(
            @PathVariable String id
    ) {
        return productService.getById(id);
    }
}