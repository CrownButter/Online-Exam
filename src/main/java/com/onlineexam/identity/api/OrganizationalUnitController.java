package com.onlineexam.identity.api;

import com.onlineexam.identity.api.dto.OrganizationalUnitCreateRequest;
import com.onlineexam.identity.api.dto.OrganizationalUnitResponse;
import com.onlineexam.identity.api.dto.OrganizationalUnitUpdateRequest;
import com.onlineexam.identity.application.service.OrganizationalUnitService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/units")
public class OrganizationalUnitController {

    private final OrganizationalUnitService organizationalUnitService;

    public OrganizationalUnitController(OrganizationalUnitService organizationalUnitService) {
        this.organizationalUnitService = organizationalUnitService;
    }

    @PostMapping
    public ResponseEntity<OrganizationalUnitResponse> create(
            @PathVariable Long organizationId,
            @Valid @RequestBody OrganizationalUnitCreateRequest request) {
        OrganizationalUnitResponse response = OrganizationalUnitResponse.from(
                organizationalUnitService.create(organizationId, request.name(), request.type(), request.parentUnitId()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{unitId}")
    public OrganizationalUnitResponse get(@PathVariable Long organizationId, @PathVariable Long unitId) {
        return OrganizationalUnitResponse.from(organizationalUnitService.get(organizationId, unitId));
    }

    @PutMapping("/{unitId}")
    public OrganizationalUnitResponse update(
            @PathVariable Long organizationId,
            @PathVariable Long unitId,
            @Valid @RequestBody OrganizationalUnitUpdateRequest request) {
        return OrganizationalUnitResponse.from(organizationalUnitService.update(
                organizationId, unitId, request.name(), request.type(), request.parentUnitId()));
    }

    @DeleteMapping("/{unitId}")
    public ResponseEntity<Void> delete(@PathVariable Long organizationId, @PathVariable Long unitId) {
        organizationalUnitService.delete(organizationId, unitId);
        return ResponseEntity.noContent().build();
    }
}
