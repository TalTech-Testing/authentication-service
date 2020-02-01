package ee.taltech.arete_admin_panel.domain;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private String salt;

    private Role role;

    public enum Role {
        USER,
        ADMIN
    }

    public User(String username, String password) {
        SHA512 sha512 = new SHA512();
        String salt = sha512.generateHash();
        String passwordHash = sha512.get_SHA_512_SecurePassword(password, salt);
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = Role.USER;
    }

}
