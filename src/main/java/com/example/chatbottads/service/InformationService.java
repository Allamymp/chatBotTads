package com.example.chatbottads.service;

import com.example.chatbottads.config.log.RedisLogger;
import com.example.chatbottads.dto.ReturnInformationDTO;
import com.example.chatbottads.exception.InformationNotFoundException;
import com.example.chatbottads.model.Information;
import com.example.chatbottads.repository.InformationRepository;
import com.example.chatbottads.util.DTOFactory;
import com.example.chatbottads.util.EncryptionUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class InformationService {

    private final RedisLogger logger;
    private final InformationRepository informationRepository;
    private final InformationCacheService informationCacheService;

    public InformationService(RedisLogger logger, InformationRepository informationRepository, InformationCacheService informationCacheService) {
        this.logger = logger;
        this.informationRepository = informationRepository;
        this.informationCacheService = informationCacheService;
    }

    @Transactional
    public ResponseEntity<URI> create(Information data) {
        logger.log("INFO", "InformationService: create() called.");
        logger.log("INFO", "InformationService: trying to create an Information with name: " + data.getName());

        try {
            Information information = informationRepository.save(new Information(data.getName(), data.getDescription()));
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(EncryptionUtil.encrypt(information.getId()))
                    .toUri();

            logger.log("INFO", "InformationService: Information created successfully with id: " + information.getId());
            informationCacheService.evictAllCacheValues(); // Evict cache after creation
            return ResponseEntity.created(location).build();
        } catch (DataIntegrityViolationException e) {
            logger.log("ERROR", "InformationService: Information creation failed due to duplicate name: " + data.getName());
            throw new IllegalArgumentException("An information with the same name already exists");
        }
    }

    @Transactional(readOnly = true)
    public ReturnInformationDTO findById(String idEncrypted) {
        logger.log("INFO", "InformationService: findById() called.");
        logger.log("INFO", "InformationService: trying to find an Information with encrypted id: " + idEncrypted);

        Long idDecrypted = EncryptionUtil.decrypt(idEncrypted);
        Optional<Information> optInformation = informationRepository.findById(idDecrypted);

        if (optInformation.isPresent()) {
            logger.log("INFO", "InformationService: Information found successfully with id: " + idDecrypted);
            return DTOFactory.fromEntity(optInformation.get());
        } else {
            logger.log("ERROR", "InformationService: Information not found for id: " + idEncrypted);
            throw new InformationNotFoundException("Information not found for id: " + idEncrypted);
        }
    }

    @Transactional(readOnly = true)
    public Page<Information> allInformations(Pageable pageable) {
        logger.log("INFO", "InformationService: allInformations() called.");
        Page<Information> informations = informationRepository.findAll(pageable);
        logger.log("INFO", "InformationService: Retrieved " + informations.getTotalElements() + " informations.");
        return informations;
    }

    @Transactional
    public ResponseEntity<Void> deleteById(String encryptedId) {
        logger.log("INFO", "InformationService: deleteById() called.");
        logger.log("INFO", "InformationService: trying to delete an Information with encrypted id: " + encryptedId);

        Long idDecrypted = EncryptionUtil.decrypt(encryptedId);
        Optional<Information> optInformation = informationRepository.findById(idDecrypted);

        if (optInformation.isPresent()) {
            informationRepository.deleteById(idDecrypted);
            logger.log("INFO", "InformationService: Information deleted successfully with id: " + idDecrypted);
            informationCacheService.evictAllCacheValues(); // Evict cache after deletion
            return ResponseEntity.ok().build();
        } else {
            logger.log("ERROR", "InformationService: Information not found for id: " + encryptedId);
            throw new InformationNotFoundException("Information not found for id: " + encryptedId);
        }
    }

    public ReturnInformationDTO findByText(String text) {
        logger.log("INFO", "InformationService: findByText() called.");
        logger.log("INFO", "InformationService: trying to find an Information with text: " + text);

        // Normalize the input text to lower case, trim it and remove special characters
        String normalizedText = normalizeText(text);
        logger.log("INFO", "InformationService: Normalized text: " + normalizedText);

        // Get all information names from the cache and normalize them
        Set<String> informationNames = informationCacheService.getAllInformationNames();
        logger.log("INFO", "InformationService: Information names from cache: " + informationNames);

        String[] words = normalizedText.split("\\s+");
        logger.log("INFO", "InformationService: Split words: " + Arrays.toString(words));

        // Check individual words in the text
        for (String word : words) {
            logger.log("INFO", "InformationService: Checking word: " + word);
            if (informationNames.contains(word)) {
                Optional<Information> optInformation = informationRepository.findByNameIgnoreCase(word);
                if (optInformation.isPresent()) {
                    logger.log("INFO", "InformationService: Information found successfully with name: " + word);
                    return DTOFactory.fromEntity(optInformation.get());
                }
            }
        }

        // Check all possible combinations of words in the text
        for (int size = 2; size <= words.length; size++) {
            for (int i = 0; i <= words.length - size; i++) {
                String phrase = String.join(" ", Arrays.copyOfRange(words, i, i + size));
                logger.log("INFO", "InformationService: Checking phrase: " + phrase);
                if (informationNames.contains(phrase)) {
                    Optional<Information> optInformation = informationRepository.findByNameIgnoreCase(phrase);
                    if (optInformation.isPresent()) {
                        logger.log("INFO", "InformationService: Information found successfully with name: " + phrase);
                        return DTOFactory.fromEntity(optInformation.get());
                    }
                }
            }
        }

        logger.log("ERROR", "InformationService: Information not found for any part of text: " + text);
        throw new InformationNotFoundException("Information not found for text: " + text);
    }

    private String normalizeText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replaceAll("[^\\p{IsAlphabetic}\\d\\s]", "").toLowerCase().trim();
    }
}
