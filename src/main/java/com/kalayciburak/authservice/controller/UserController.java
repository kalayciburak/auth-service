package com.kalayciburak.authservice.controller;

import com.kalayciburak.authservice.model.dto.request.ChangePasswordRequest;
import com.kalayciburak.authservice.service.UserService;
import com.kalayciburak.commonpackage.core.response.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/user", produces = "application/json")
@Tag(name = "Kullanıcı Yetkilendirme & Yönetimi", description = "Kullanıcı yetkilendirme ve yönetimi işlemleri")
public class UserController {
    private final UserService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Tüm kullanıcıları getir",
            description = "Sadece ADMIN rolüne sahip kullanıcılar tarafından çağrılabilir."
    )
    public Response getAllUsers() {
        return service.getAllUsers();
    }

    @PutMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @userAuthorizationHelper.isCurrentUser(#id)")
    @Operation(
            summary = "Kullanıcı parolasını değiştir",
            description = "Sadece ADMIN veya kendi parolasını değiştiren kullanıcı tarafından kullanılabilir."
    )
    public Response changePassword(@PathVariable Long id, @RequestBody @Valid ChangePasswordRequest request) {
        return service.changePassword(id, request);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Kullanıcı rollerini güncelle",
            description = "Sadece ADMIN kullanıcılar tarafından çağrılabilir."
    )
    public Response updateUserRoles(@PathVariable Long id, @RequestBody Set<Long> roleIds) {
        return service.updateUserRoles(id, roleIds);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Kullanıcıyı sil",
            description = "Sadece ADMIN kullanıcılar tarafından çağrılabilir. ADMIN kullanıcıları silmek mümkün değildir."
    )
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }
}
