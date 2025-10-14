package com.soilidstate.api.repository;


import com.soilidstate.api.entity.PhidgetConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhidgetConnectionRepository extends JpaRepository<PhidgetConnection, Long> {
    List<PhidgetConnection> findByUserId(Long userId);

    Optional<PhidgetConnection> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT c FROM PhidgetConnection c WHERE c.user.id = :userId AND c.isActive = true")
    Optional<PhidgetConnection> findActiveConnectionByUserId(@Param("userId") Long userId);

    List<PhidgetConnection> findByUserIdOrderByCreatedAtDesc(Long userId);
}
