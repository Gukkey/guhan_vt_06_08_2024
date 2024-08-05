package com.gukkey.urlshortner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gukkey.urlshortner.domain.ShortURL;

@Repository
public interface DBRepository extends JpaRepository<ShortURL, Long>{
    ShortURL findByDestinationURL(String destinationURL);
    ShortURL findByShortLink(String shortLink);
}
