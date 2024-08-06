package com.gukkey.urlshortner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gukkey.urlshortner.domain.ShortURL;
import com.gukkey.urlshortner.model.DTO;
import com.gukkey.urlshortner.model.res.Response;
import com.gukkey.urlshortner.repository.DBRepository;

@ExtendWith(MockitoExtension.class)
public class ShortURLServiceTest {

    @Mock
    private DBRepository dbRepository;

    @InjectMocks
    ShortURLService shortURLService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("createShortLinkInputs")
    void testCreatingShortLink(String input) {
        DTO dto = new DTO();
        dto.setDestinationURL(input);

        when(dbRepository.findByDestinationURL(any())).thenReturn(null);
        when(dbRepository.findByShortLink(any())).thenReturn(null);

        ResponseEntity<Response> responseEntity = shortURLService.createShortLink(dto);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.valueOf(201), responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals("Added", response.getMessage());
        assertTrue(response.getShortURL().startsWith("http://localhost:8080/api/v1"));
        assertEquals(LocalDate.now().plusDays(304), response.getExpireAt());

        verify(dbRepository).findByDestinationURL(anyString());
    }

    @ParameterizedTest
    @MethodSource("isValidDestinationURLInputs")
    void testIsValidDestinationURL(String input) {
        Boolean output = shortURLService.isValidDestinationURL(input);
        assertTrue(output);
    }

    @ParameterizedTest
    @MethodSource("updateShortLinkInputs")
    void testUpdatingShortLink(DTO dto) {
        ShortURL shortURL = ShortURL.builder().destinationURL("facebook.com").expireAt(LocalDate.now().plusDays(90)).shortLink("qwertyui").build();
        
        when(dbRepository.findByShortLink(any())).thenReturn(shortURL);

        // Since we are going to not check whether the shortlink exist, might well as given some random string
        String shortlink = "quertyui";

        ResponseEntity<Response> responseEntity = shortURLService.updateShortLink(shortlink, dto);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());

        if (dto.getDestinationURL() != null) {
            if (shortURLService.isValidDestinationURL(dto.getDestinationURL())) {
                // Use the actual stripProtocol method here if needed
                String expectedUrl = shortURLService.stripProtocol(dto.getDestinationURL());
                assertEquals(expectedUrl, shortURL.getDestinationURL());
            } else {
                assertEquals(HttpStatus.valueOf(400), responseEntity.getStatusCode());
                assertTrue(responseEntity.getBody().getMessage().contains("This destination URL is not valid"));
            }
        }

        if (dto.getDays() > 0) {
           assertEquals(LocalDate.now().plusDays(90).plusDays(dto.getDays()), shortURL.getExpireAt());
        }

        if (responseEntity.getStatusCode().equals(HttpStatus.valueOf(202))) {
            verify(dbRepository).save(shortURL);
        }
    }

    private static Stream<String> createShortLinkInputs() throws IOException, URISyntaxException {
        ClassLoader classLoader = ShortURLServiceTest.class.getClassLoader();
        String filePath = "inputs/create_short_links_input.txt";
        return Files.lines(Paths.get(classLoader.getResource(filePath).toURI()));
    }

    private static Stream<String> isValidDestinationURLInputs() throws IOException, URISyntaxException {
        ClassLoader classLoader = ShortURLServiceTest.class.getClassLoader();
        String filePath = "inputs/is_valid_destination_url_input.txt";
        return Files.lines(Paths.get(classLoader.getResource(filePath).toURI()));
    }

    private static Stream<DTO>  updateShortLinkInputs() throws IOException {
        List<DTO> testCases = new ArrayList<>();
        
        try(InputStream inputStream = ShortURLServiceTest.class.getResourceAsStream("/inputs/update_short_links_input.json")) {
            JsonNode jsonNode = objectMapper.readTree(inputStream);
            JsonNode inputs = jsonNode.get("inputs");

            for(JsonNode input : inputs) {
                String destinationURL = input.has("destinationURL") ? input.get("destinationURL").asText() : null;
                long days = input.has("days") ? input.get("days").asLong() : 0;
                testCases.add(new DTO(destinationURL, days));
            }
        }
        return testCases.stream();
    }
}
