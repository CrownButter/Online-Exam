package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.RolePermission;
import com.onlineexam.identity.domain.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}
