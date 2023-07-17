package ru.skypro.homework.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PHOTO")
public class Photo extends Image {
    public Photo() {
    }

    public Photo(String dir) {
        this.setImageDir(dir);
    }
}
