package ru.skypro.homework.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("AVATAR")
public class Avatar extends Image {
    public Avatar() {
    }

    public Avatar(String dir) {
        this.setImageDir(dir);
    }
}
