package com.example.chatbottads.service;

import com.example.chatbottads.config.log.RedisLogger;
import com.example.chatbottads.model.Information;
import com.example.chatbottads.repository.InformationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InformationCacheService {

    private final InformationRepository informationRepository;
    private final RedisLogger logger;

    public InformationCacheService(InformationRepository informationRepository, RedisLogger logger) {
        this.informationRepository = informationRepository;
        this.logger = logger;
    }

    @Cacheable("informationNames")
    public Set<String> getAllInformationNames() {
        Set<String> names = informationRepository.findAll().stream()
                .map(Information::getName)
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());
        logger.log("INFO", "InformationCacheService: Cached information names: " + names);
        return names;
    }

    @CacheEvict(value = "informationNames", allEntries = true)
    public void evictAllCacheValues() {
        logger.log("INFO", "InformationCacheService: Cache evicted.");
    }

    @CacheEvict(value = "informationNames", allEntries = true)
    @Cacheable("informationNames")
    public void refreshAllInformationNames() {
        getAllInformationNames();
    }
}
