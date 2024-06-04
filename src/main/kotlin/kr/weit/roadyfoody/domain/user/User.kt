package kr.weit.roadyfoody.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.roadyfoody.domain.BaseModifiableEntity
import kr.weit.roadyfoody.support.regex.NICKNAME_REGEX
import kr.weit.roadyfoody.support.regex.NICKNAME_REGEX_DESC

@Entity
@Table(name = "users")
@SequenceGenerator(name = "USERS_SEQ_GENERATOR", sequenceName = "USERS_SEQ", initialValue = 1, allocationSize = 1)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_SEQ_GENERATOR")
    @Column(name = "id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val id: Long = 0L,
    @Column(name = "nickname", nullable = false, unique = true)
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
