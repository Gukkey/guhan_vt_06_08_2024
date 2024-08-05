package com.gukkey.urlshortner.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class Controller {

    @Autowired
    ShortURLService shortURLService;

    @PostMapping("/create")
    public ResponseEntity<Response> createShortLink(@RequestBody DTO dto) {
        return shortURLService.createShortLink(dto);
    }

    @PutMapping("/edit/{shorturl}")
    public ResponseEntity<Response> updateShortLink(@RequestBody DTO dto, @PathVariable String shorturl) {
        return shortURLService.updateShortLink(shorturl, dto);
    }

    @GetMapping("/{shorturl}")
    public ResponseEntity<Response> getDestinationLink(@PathVariable String shorturl) {
        return shortURLService.getDestinationURL(shorturl);
    }
}
