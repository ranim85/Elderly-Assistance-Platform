package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.domain.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByRole(Role role);

    @Modifying
    @Query("update User u set u.linkedElderlyPerson = null where u.linkedElderlyPerson is not null")
    void clearLinkedElderlyReferences();
}
