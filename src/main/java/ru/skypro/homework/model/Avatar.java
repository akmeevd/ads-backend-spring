package ru.skypro.homework.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
@DiscriminatorValue("AVATAR")
public class Avatar extends Photo {
    public Avatar() {
    }

    public Avatar(String dir) {
        this.setPhotoDir(dir);
    }

    @Override
    public Path getFilePath() {
        return Paths.get(this.getPhotoDir(), this.getId() + "." + this.getFileExtension());
    }
}
