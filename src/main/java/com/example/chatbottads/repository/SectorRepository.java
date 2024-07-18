package com.example.chatbottads.repository;


import com.example.chatbottads.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    Optional<Sector> findByNameIgnoreCase(String name);
}
