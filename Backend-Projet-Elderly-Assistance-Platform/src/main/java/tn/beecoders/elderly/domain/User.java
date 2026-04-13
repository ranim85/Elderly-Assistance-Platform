package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(length = 120)
    private String password;

    /**
     * Stored as enum name (e.g. {@code FAMILY_MEMBER}). Must allow at least the longest role name
     * so MySQL/Hibernate do not truncate or reject inserts (a common cause of 500 for family accounts).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 32, nullable = false)
    private Role role;

    /**
     * For {@link Role#FAMILY_MEMBER}: elderly person this account is linked to.
     * EAGER so {@code UserController#mapToDTO} can read the id after the creating transaction ends (LAZY would cause LazyInitializationException for family accounts only).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linked_elderly_person_id")
    private ElderlyPerson linkedElderlyPerson;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
