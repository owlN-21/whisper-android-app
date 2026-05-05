package org.example.audiosummary.user.repository;

import java.util.Optional;
import org.example.audiosummary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
