package com.ndgl.spotfinder.domain.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.admin.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
	public Optional<Admin> findByUsername(String username);
	public boolean existsAdminByUsername(String username);
}
