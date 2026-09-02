package com.example.app.model.enums;

/**
 * Application roles. Prefixed with ROLE_ nowhere here — Spring Security's
 * hasRole()/hasAnyRole() add the "ROLE_" prefix automatically when we use
 * the enum name directly in GrantedAuthority construction.
 */
public enum Role {
    ADMIN,
    RECEPTIONIST,
    CUSTOMER
}
