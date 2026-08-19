package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.Permission;
import com.onlineexam.identity.domain.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission get(Long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
    }
}
