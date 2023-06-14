package ru.skypro.homework.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
public abstract class Photo {
    public enum PhotoType {AVATAR, IMAGE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    @Enumerated(EnumType.STRING)
    private PhotoType photoType;
    private String photoDir;
    private String fileType;
    private String fileName;
    private String fileExtension;
    private long fileSize;

    public abstract Path getFilePath();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return id == photo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
