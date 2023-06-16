package ru.skypro.homework.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
@Table(name = "photos")
public class Avatar extends Photo {
    public Avatar() {
    }

    public Avatar(String dir) {
        this.setPhotoDir(dir);
        this.setPhotoType(PhotoType.AVATAR);
    }

    @Override
    public Path getFilePath() {
        return Paths.get(this.getPhotoDir(), this.getId() + "." + this.getFileExtension());
    }
}
