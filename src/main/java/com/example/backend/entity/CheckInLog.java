package com.example.backend.entity;

import com.example.backend.enums.CheckInMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checkin_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CheckInLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_in_by", nullable = false)
    private User checkedInBy;

    @Enumerated(EnumType.STRING)
    private CheckInMethod method;
}
