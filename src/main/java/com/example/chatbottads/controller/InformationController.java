package com.example.chatbottads.controller;

import com.example.chatbottads.config.log.RedisLogger;
import com.example.chatbottads.dto.ReturnInformationDTO;
import com.example.chatbottads.model.Information;
import com.example.chatbottads.service.InformationService;
import com.example.chatbottads.util.DTOFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/informations")
@CrossOrigin("*")
public class InformationController {

    private final InformationService informationService;
    private final RedisLogger logger;
    private final PagedResourcesAssembler<ReturnInformationDTO> pagedResourcesAssembler;

    @Autowired
    public InformationController(InformationService informationService,
                                 RedisLogger logger,
                                 PagedResourcesAssembler<ReturnInformationDTO> pagedResourcesAssembler) {
        this.informationService = informationService;
        this.logger = logger;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping
    public ResponseEntity<URI> createInformation(@Valid @RequestBody Information information) {
        logger.log("INFO", "InformationController: createSector() called.");
        logger.log("INFO", "InformationController: creating information with name: " + information.getName());
        return informationService.create(information);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnInformationDTO> getInformationById(@PathVariable String id) {
        logger.log("INFO", "InformationController: getSectorById() called with id: " + id);
        return ResponseEntity.ok(informationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ReturnInformationDTO>>> getAllInformations(Pageable pageable) {
        logger.log("INFO", "InformationController: getAllSectors() called.");
        Page<ReturnInformationDTO> sectors = informationService.allInformations(pageable).map(DTOFactory::fromEntity);
        PagedModel<EntityModel<ReturnInformationDTO>> pagedModel = pagedResourcesAssembler.toModel(sectors);
        return ResponseEntity.ok(pagedModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInformationById(@PathVariable String id) {
        logger.log("INFO", "InformationController: deleteSectorById() called with id: " + id);
        return informationService.deleteById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<ReturnInformationDTO> findByText(@RequestParam String text) {
        logger.log("INFO", "InformationController: findByText() called with text: " + text);
        return ResponseEntity.ok(informationService.findByText(text));
    }
}
