package com.example.chatbottads.repository;


import com.example.chatbottads.model.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {

    Optional<Information> findByNameIgnoreCase(String name);
}
