package ru.skypro.homework.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
@DiscriminatorValue("IMAGE")
public class Image extends Photo {
    public Image() {
    }

    public Image(String dir) {
        this.setPhotoDir(dir);
    }

    @Override
    public Path getFilePath() {
        return Paths.get(this.getPhotoDir(), this.getId() + "." + this.getFileExtension());
    }
}
