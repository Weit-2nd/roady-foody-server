package kr.weit.roadyfoody.auth.security

import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.repository.UserRepository
import kr.weit.roadyfoody.user.repository.getBySocialId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val userRepository: UserRepository,
) :
    UserDetailsService {
    override fun loadUserByUsername(socialId: String): UserDetails {
        return SecurityUser(userRepository.getBySocialId(socialId))
    }
}

class SecurityUser(val user: User) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf()
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return ""
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
