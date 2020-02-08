package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.taltech.arete_admin_panel.algorithms.SHA512;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@Builder
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String username;

    private String color = "general";

    @NotNull
    @Column(name = "password_hash")
    private String passwordHash;

    @NotNull
    private String salt;

    @NotNull
    private Role role;

    public User(String username, String password) {
        SHA512 sha512 = new SHA512();
        String salt = sha512.generateHash();
        String passwordHash = sha512.get_SHA_512_SecurePassword(password, salt);
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = Role.USER;
    }

    public enum Role {
        USER,
        ADMIN
    }

}
