package ru.skypro.homework.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity(name = "images")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "image_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    private String imageDir;
    private String fileType;
    private String fileName;
    private String fileExtension;
    private long fileSize;

    public Path getFilePath() {
        return Paths.get(this.getImageDir(), this.getId() + "." + this.getFileExtension());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return id == image.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
