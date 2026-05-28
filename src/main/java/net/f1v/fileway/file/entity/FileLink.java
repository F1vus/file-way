package net.f1v.fileway.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.f1v.fileway.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "file_link")
@Getter
@Setter
@NoArgsConstructor
public class FileLink {

    @Id
    @Column(name = "file_link_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "file_link_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "file_link_expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "file_link_max_uses", nullable = false)
    private int maxUses = 1;

    @Column(name = "file_link_use_count", nullable = false)
    private int useCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "link")
    private File file;

    public FileLink(UUID id, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}