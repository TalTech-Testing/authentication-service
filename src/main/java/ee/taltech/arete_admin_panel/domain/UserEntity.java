package ee.taltech.arete_admin_panel.domain;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Column(name = "user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String username;

    private String color = "general";

    @NotNull
    @Column(name = "password_hash")
    private String passwordHash;

    @NotNull
    private String salt;

    @NotNull
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
    private List<Role> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(Role::toValue).map(SimpleGrantedAuthority::new).collect(toList());
    }

    @Deprecated
    @Override
    public String getPassword() {
        return null;
    }

    @Deprecated
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isEnabled() {
        return false;
    }


    public UserEntity(String username, String password) {
        SHA512 sha512 = new SHA512();
        String salt = sha512.generateHash();
        String passwordHash = sha512.get_SHA_512_SecurePassword(password, salt);
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
		this.roles = new ArrayList<>();
        this.roles.add(Role.USER);
    }

	public UserEntity(String username, String password, Role role) {
		SHA512 sha512 = new SHA512();
		String salt = sha512.generateHash();
		String passwordHash = sha512.get_SHA_512_SecurePassword(password, salt);
		this.username = username;
		this.passwordHash = passwordHash;
		this.salt = salt;
		this.roles = new ArrayList<>();
		this.roles.add(role);
	}

}
