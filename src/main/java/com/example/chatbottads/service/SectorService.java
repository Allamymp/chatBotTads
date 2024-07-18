package com.example.chatbottads.service;

import com.example.chatbottads.config.log.RedisLogger;
import com.example.chatbottads.dto.ReturnSectorDTO;
import com.example.chatbottads.exception.SectorNotFoundException;
import com.example.chatbottads.model.Sector;
import com.example.chatbottads.repository.SectorRepository;
import com.example.chatbottads.util.DTOFactory;
import com.example.chatbottads.util.EncryptionUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SectorService {

    private final RedisLogger logger;
    private final SectorRepository sectorRepository;


    private final SectorService self;

    public SectorService(RedisLogger logger, SectorRepository sectorRepository, SectorService self) {
        this.logger = logger;
        this.sectorRepository = sectorRepository;
        this.self = self;
    }

    @Transactional
    public ResponseEntity<URI> create(Sector data) {
        logger.log("INFO", "SectorService: create() called.");
        logger.log("INFO", "SectorService: trying to create a Sector with name: " + data.getName());

        Sector sector = sectorRepository.save(new Sector(data.getName(), data.getDescription()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(sector.getId())
                .toUri();

        logger.log("INFO", "SectorService: Sector created successfully with id: " + sector.getId());
        return ResponseEntity.created(location).build();
    }

    @Transactional(readOnly = true)
    public Sector findById(String idEncrypted) {
        logger.log("INFO", "SectorService: findById() called.");
        logger.log("INFO", "SectorService: trying to find a Sector with encrypted id: " + idEncrypted);

        Long idDecrypted = EncryptionUtil.decrypt(idEncrypted);
        Optional<Sector> optSector = sectorRepository.findById(idDecrypted);

        if (optSector.isPresent()) {
            logger.log("INFO", "SectorService: Sector found successfully with id: " + idDecrypted);
            return optSector.get();
        } else {
            logger.log("ERROR", "SectorService: Sector not found for id: " + idEncrypted);
            throw new SectorNotFoundException("Sector not found for id: " + idEncrypted);
        }
    }

    @Transactional(readOnly = true)
    public Page<Sector> allSectors(Pageable pageable) {
        logger.log("INFO", "SectorService: allSectors() called.");
        Page<Sector> sectors = sectorRepository.findAll(pageable);
        logger.log("INFO", "SectorService: Retrieved " + sectors.getTotalElements() + " sectors.");
        return sectors;
    }

    @Transactional
    public ResponseEntity<Void> deleteById(String encryptedId) {
        logger.log("INFO", "SectorService: deleteById() called.");
        logger.log("INFO", "SectorService: trying to delete a Sector with encrypted id: " + encryptedId);

        Long idDecrypted = EncryptionUtil.decrypt(encryptedId);
        Optional<Sector> optSector = sectorRepository.findById(idDecrypted);

        if (optSector.isPresent()) {
            sectorRepository.deleteById(idDecrypted);
            logger.log("INFO", "SectorService: Sector deleted successfully with id: " + idDecrypted);
            return ResponseEntity.ok().build();
        } else {
            logger.log("ERROR", "SectorService: Sector not found for id: " + encryptedId);
            throw new SectorNotFoundException("Sector not found for id: " + encryptedId);
        }
    }

    @Cacheable("sectorNames")
    public Set<String> getAllSectorNames() {
        logger.log("INFO", "SectorService: getAllSectorNames() called.");
        List<Sector> sectors = sectorRepository.findAll();
        Set<String> sectorNames = sectors.stream()
                .map(sector -> sector.getName().toLowerCase())
                .collect(Collectors.toSet());
        logger.log("INFO", "SectorService: Cached " + sectorNames.size() + " sector names.");
        return sectorNames;
    }

    @Transactional(readOnly = true)
    public ReturnSectorDTO findByText(String text) {
        logger.log("INFO", "SectorService: findByText() called.");
        logger.log("INFO", "SectorService: trying to find a Sector with text: " + text);

        Set<String> sectorNames = self.getAllSectorNames();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (sectorNames.contains(word.toLowerCase())) {
                Optional<Sector> optSector = sectorRepository.findByNameIgnoreCase(word);
                if (optSector.isPresent()) {
                    logger.log("INFO", "SectorService: Sector found successfully with name: " + word);
                    return DTOFactory.fromEntity(optSector.get());
                }
            }
        }

        logger.log("ERROR", "SectorService: Sector not found for any part of text: " + text);
        throw new SectorNotFoundException("Sector not found for text: " + text);
    }
}
