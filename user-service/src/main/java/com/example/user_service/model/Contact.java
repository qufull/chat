package com.example.user_service.model;

import com.example.user_service.model.enums.ContactStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "contacts")
public class Contact {
    @Id
    private UUID userId;
    @Id
    private UUID contactId;

    @Enumerated(EnumType.STRING)
    private ContactStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Timestamp createdAt;

}
