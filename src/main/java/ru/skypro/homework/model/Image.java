package ru.skypro.homework.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
@Table(name = "photos")
public class Image extends Photo {
    public Image() {
    }

    public Image(String dir) {
        this.setPhotoDir(dir);
        this.setPhotoType(PhotoType.IMAGE);
    }

    @Override
    public Path getFilePath() {
        return Paths.get(this.getPhotoDir(), this.getId() + "." + this.getFileExtension());
    }
}
