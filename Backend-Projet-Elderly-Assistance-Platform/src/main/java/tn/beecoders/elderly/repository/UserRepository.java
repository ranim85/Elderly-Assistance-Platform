package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.beecoders.elderly.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
