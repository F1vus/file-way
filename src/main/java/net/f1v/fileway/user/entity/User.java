package net.f1v.fileway.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.f1v.fileway.file.entity.File;
import net.f1v.fileway.file.entity.FileLink;

import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_email", nullable = false, length = 300)
    private String email;

    @Column(name = "user_password", nullable = false, length = 512)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<FileLink> userFileLinks;

    @OneToMany(mappedBy = "user")
    private List<File> userFiles;
}