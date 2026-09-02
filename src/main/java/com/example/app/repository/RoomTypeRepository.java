package com.example.app.repository;

import com.example.app.model.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByName(String name);

    boolean existsByName(String name);
}
