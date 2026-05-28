package net.f1v.fileway.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.f1v.fileway.user.entity.User;

import java.time.LocalDateTime;


@Entity
@Table(name = "file")
@Getter
@Setter
@NoArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="file_id")
    private Long id;

    @Column(name = "file_size", nullable = false)
    private Long size;

    @Column(name = "file_storage_name", nullable = false)
    private String storageName;

    @Column(name = "file_created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "file_original_name", nullable = false)
    private String originalName;

    @Column(name = "file_hash", nullable = false)
    private String fileHash;

    @OneToOne
    @JoinColumn(name = "file_link_id")
    private FileLink link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public File(Long size, String storageName, LocalDateTime createdAt, String originalName, FileLink link) {
        this.size = size;
        this.createdAt = createdAt;
        this.storageName = storageName;
        this.originalName = originalName;
        this.link = link;
    }
}
