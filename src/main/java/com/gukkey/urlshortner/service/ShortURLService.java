package com.gukkey.urlshortner.service;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.gukkey.urlshortner.domain.ShortURL;
import com.gukkey.urlshortner.model.DTO;
import com.gukkey.urlshortner.model.res.Response;
import com.gukkey.urlshortner.repository.DBRepository;

@Service
public class ShortURLService {

    Logger LOGGER = LoggerFactory.getLogger(ShortURLService.class); 

    private DBRepository dbRepository;

    @Autowired
    private ShortURLService(DBRepository dbRepository) {
        this.dbRepository = dbRepository;
    }

    public ResponseEntity<Response> createShortLink(final DTO dto) {

        String destinationURL = dto.getDestinationURL();

        // rip the protocol (www, http, https)
        destinationURL = stripProtocol(destinationURL);
        final Response response = Response.builder().status(400).message("This destination URL is already present").build();

        // check whether the destination url is already present in the database
        final ShortURL previousURL = dbRepository.findByDestinationURL(destinationURL);
        if(previousURL != null) {
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        // generate randomized value
        String randomziedValue = UUID.randomUUID().toString().substring(0, 8);
        while(dbRepository.findByShortLink(randomziedValue) != null) {
            randomziedValue = UUID.randomUUID().toString().substring(0, 8);
        }

        final LocalDate expireAt= LocalDate.now().plusDays(304);

        // add in the database
        final ShortURL shortURL = new ShortURL();
        shortURL.setDestinationURL(destinationURL);
        shortURL.setShortLink(randomziedValue);
        shortURL.setExpireAt(expireAt);

        dbRepository.save(shortURL);

        // create response object
        response.setStatus(201);
        response.setMessage("Added");
        response.setShortURL("http://localhost:8080/api/v1/" + randomziedValue);
        response.setExpireAt(expireAt);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<Response> updateShortLink(final String shortlink, final DTO dto){
        final ShortURL shortURL = dbRepository.findByShortLink(shortlink);
        final Response response = Response.builder().status(404).message("Given short link is not present").build();

        if(shortURL == null) {
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        if(dto.getDestinationURL() != null) {
            final String destinationURL = dto.getDestinationURL();
            shortURL.setDestinationURL(destinationURL);
            response.setDestinationURL(destinationURL);
        }

        if(dto.getDays() > 0) {
            final long expireAt = dto.getDays();
            final LocalDate currentLocalDate = shortURL.getExpireAt().plusDays(expireAt);
            shortURL.setExpireAt(currentLocalDate);
            response.setExpireAt(currentLocalDate);
        }

        dbRepository.save(shortURL);
        response.setStatus(202);
        response.setMessage("Requested changes has been accepted");
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<Response> getDestinationURL(final String shortLink) {        
        LOGGER.info("Processing short link: {}", shortLink);
        final var shortURL = dbRepository.findByShortLink(shortLink);

        if (shortURL == null) {
            LOGGER.warn("Short link not found: {}", shortLink);
            final Response response = Response.builder()
                    .status(404)
                    .message("Link not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        final String destinationURL = shortURL.getDestinationURL();
        LOGGER.info("Redirecting {} to {}", shortLink, destinationURL);
        return ResponseEntity.status(301)
                .location(URI.create("http://" + destinationURL))
                .build();
    }

    private static String stripProtocol(final String destinationURL) {
        return destinationURL.replaceAll("^(https?://|www\\.)", "");
    }

}
