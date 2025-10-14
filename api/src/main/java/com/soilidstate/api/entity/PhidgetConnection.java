package com.soilidstate.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Phidget Connection Entity
@Entity
@Table(name = "phidget_connections", indexes = {
        @Index(name = "idx_connection_user", columnList = "user_id"),
        @Index(name = "idx_connection_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhidgetConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "server_address", nullable = false)
    private String serverAddress;

    @Column(name = "server_port", nullable = false)
    private Integer serverPort;

    @Column(name = "phidget_port")
    private Integer phidgetPort; // Port to connect to PhidgetSBC4

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "connection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RegisteredSensor> sensors = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
