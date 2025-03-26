package com.ndgl.spotfinder.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.user.entity.User;

@Repository
public interface UserLoginRepository extends JpaRepository<User, Long> {
}
