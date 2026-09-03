package com.luemi.porcicola.repository;

import com.luemi.porcicola.model.Granja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GranjaRepository extends JpaRepository<Granja, Integer> {
}
