package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.AppUser;
import com.onlineexam.identity.domain.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AppUser get(Long organizationId, Long userId) {
        return appUserRepository.findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public AppUser getByEmail(Long organizationId, String email) {
        return appUserRepository.findByOrganization_IdAndEmail(organizationId, email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(Long organizationId, String username) {
        return appUserRepository.findByOrganization_IdAndUsername(organizationId, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
    }
}
