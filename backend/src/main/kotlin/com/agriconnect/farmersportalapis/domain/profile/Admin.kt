package com.agriconnect.farmersportalapis.domain.profile

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*

@Entity
@Table(name = "admins")
class Admin(
    @Id
    @Column(name = "id", length = 36)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var userProfile: UserProfile,

    @Column(name = "role", length = 50)
    var role: String? = null,

    @Column(name = "department", length = 100)
    var department: String? = null,
)