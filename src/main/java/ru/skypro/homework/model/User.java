package ru.skypro.homework.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Avatar avatar;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "enabled")
    private boolean isEnabled;

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private Set<Advert> adverts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
