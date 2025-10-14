package com.soilidstate.api.repository;


import com.soilidstate.api.entity.RegisteredSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredSensorRepository extends JpaRepository<RegisteredSensor, Long> {
    List<RegisteredSensor> findByConnectionId(Long connectionId);

    Optional<RegisteredSensor> findByPhidgetSensorId(String phidgetSensorId);

    Optional<RegisteredSensor> findByConnectionIdAndPhidgetSensorId(Long connectionId, String phidgetSensorId);

    @Query("SELECT s FROM RegisteredSensor s WHERE s.connection.id = :connectionId ORDER BY s.sensorName")
    List<RegisteredSensor> findByConnectionIdOrderBySensorName(@Param("connectionId") Long connectionId);

    void deleteByPhidgetSensorId(String phidgetSensorId);
}