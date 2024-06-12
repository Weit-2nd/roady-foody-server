package kr.weit.roadyfoody.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.roadyfoody.common.domain.BaseModifiableEntity
import kr.weit.roadyfoody.user.utils.NICKNAME_REGEX
import kr.weit.roadyfoody.user.utils.NICKNAME_REGEX_DESC

@Entity
@Table(name = "users")
@SequenceGenerator(name = "USERS_SEQ_GENERATOR", sequenceName = "USERS_SEQ", initialValue = 1, allocationSize = 1)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_SEQ_GENERATOR")
    @Column(name = "id", updatable = false, nullable = false)
    val id: Long = 0L,
    @Column(name = "nickname", length = 48, nullable = false, unique = true)
    var nickname: String,
) :
    BaseModifiableEntity() {
    init {
        require(NICKNAME_REGEX.matches(nickname)) { NICKNAME_REGEX_DESC }
    }

    fun changeNickname(nickname: String) {
        require(NICKNAME_REGEX.matches(nickname)) { NICKNAME_REGEX_DESC }
        this.nickname = nickname
    }
}