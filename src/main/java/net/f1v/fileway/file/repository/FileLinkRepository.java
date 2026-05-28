package net.f1v.fileway.file.repository;

import net.f1v.fileway.file.entity.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileLinkRepository  extends JpaRepository<FileLink, UUID> {
}
