package com.example.chatbottads.controller;

import com.example.chatbottads.config.log.RedisLogger;
import com.example.chatbottads.dto.ReturnSectorDTO;
import com.example.chatbottads.model.Sector;
import com.example.chatbottads.service.SectorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/sectors")
@CrossOrigin("*")
public class SectorController {

    private final SectorService sectorService;
    private final RedisLogger logger;

    @Autowired
    public SectorController(SectorService sectorService, RedisLogger logger) {
        this.sectorService = sectorService;
        this.logger = logger;
    }

    @PostMapping
    public ResponseEntity<URI> createSector(@Valid @RequestBody Sector sector) {
        logger.log("INFO", "SectorController: createSector() called.");
        logger.log("INFO", "SectorController: creating sector with name: " + sector.getName());
        return sectorService.create(sector);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sector> getSectorById(@PathVariable String id) {
        logger.log("INFO", "SectorController: getSectorById() called with id: " + id);
        return ResponseEntity.ok(sectorService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Sector>> getAllSectors(Pageable pageable) {
        logger.log("INFO", "SectorController: getAllSectors() called.");
        return ResponseEntity.ok(sectorService.allSectors(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSectorById(@PathVariable String id) {
        logger.log("INFO", "SectorController: deleteSectorById() called with id: " + id);
        return sectorService.deleteById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<ReturnSectorDTO> findByText(@RequestParam String text) {
        logger.log("INFO", "SectorController: findByText() called with text: " + text);
        return ResponseEntity.ok(sectorService.findByText(text));
    }
}
