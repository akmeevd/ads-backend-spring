package ru.skypro.homework.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.Objects;

@Getter
@Setter
@Entity(name="photos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="photo_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
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
