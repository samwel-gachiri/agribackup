package com.agriconnect.farmersportalapis.domain.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserProfile(
    @Id
    @JsonIgnore
    @Column(length = 36)
    var id: String = "",

    @Column(unique = true, nullable = true)
    var email: String?,

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    var passwordHash: String,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @JsonIgnore
    @Column(name = "is_active")
    var isActive: Boolean = true,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @CreationTimestamp
    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @JsonIgnore
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "roles")
class Role(
    @Id
    @Column(length = 36)
    var id: String = "",

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var description: String? = null,

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    var userProfiles: MutableSet<UserProfile> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    var permissions: MutableSet<Permission> = mutableSetOf()
)

@Entity
@Table(name = "permissions")
class Permission(
    @Id
    @Column(length = 36)
    var id: String = "",

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var description: String? = null,

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    var roles: MutableSet<Role> = mutableSetOf()
)

@Entity
@Table(name = "password_reset_tokens")
data class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val token: String,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserProfile,

    @Column(nullable = false)
    val expiryDate: LocalDateTime
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiryDate)
}
