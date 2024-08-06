package com.gukkey.urlshortner.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gukkey.urlshortner.model.DTO;
import com.gukkey.urlshortner.model.res.Response;
import com.gukkey.urlshortner.service.ShortURLService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class Controller {

    private ShortURLService shortURLService;
    private Bucket bucket;
    private static final String TOO_MANY_REQUESTS = "Too many requests, try again after a minute";

    @Autowired
    Controller(ShortURLService shortURLService) {
        long capacity = 10;
        Bandwidth limit = Bandwidth.builder().capacity(capacity).refillGreedy(capacity, Duration.ofMinutes(1)).build();

        this.shortURLService = shortURLService;
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @PostMapping("/create")
    public ResponseEntity<Response> createShortLink(@RequestBody DTO dto) {
        if (bucket.tryConsume(1)) {
            return shortURLService.createShortLink(dto);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Response.builder().status(HttpStatus.TOO_MANY_REQUESTS.value()).message(TOO_MANY_REQUESTS).build());
        }
    }

    @PutMapping("/edit/{shorturl}")
    public ResponseEntity<Response> updateShortLink(@RequestBody DTO dto, @PathVariable String shorturl) {
        if (bucket.tryConsume(1)) {
            return shortURLService.updateShortLink(shorturl, dto);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Response.builder().status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message(TOO_MANY_REQUESTS).build());
        }
    }

    @GetMapping("/{shorturl}")
    public ResponseEntity<Response> getDestinationLink(@PathVariable String shorturl) {
        if (bucket.tryConsume(1)) {
            return shortURLService.getDestinationURL(shorturl);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Response.builder().status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message(TOO_MANY_REQUESTS).build());
        }
    }

    @GetMapping("/stats/{shorturl}")
    public ResponseEntity<Response> getStats(@PathVariable String shorturl) {
        if (bucket.tryConsume(1)) {
            return shortURLService.getStats(shorturl);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Response.builder().status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message(TOO_MANY_REQUESTS).build());
        }
    }
}
