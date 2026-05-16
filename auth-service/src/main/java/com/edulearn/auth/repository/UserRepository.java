package com.edulearn.auth.repository;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    List<User> findByFullNameContaining(String fullName);
}
