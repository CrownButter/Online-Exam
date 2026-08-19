package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByCode(String code);
}
