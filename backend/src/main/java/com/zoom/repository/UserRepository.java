package com.zoom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zoom.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByZoomUserId(String zoomUserId);

    Optional<User> findByZoomAccountId(String zoomAccountId);

    boolean existsByUsername(String username);
}
