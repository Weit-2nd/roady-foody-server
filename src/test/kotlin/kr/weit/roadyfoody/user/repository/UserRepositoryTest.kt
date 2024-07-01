package kr.weit.roadyfoody.user.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kr.weit.roadyfoody.support.annotation.RepositoryTest
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.exception.UserNotFoundException
import kr.weit.roadyfoody.user.fixture.TEST_NONEXISTENT_ID
import kr.weit.roadyfoody.user.fixture.TEST_NONEXISTENT_NICKNAME
import kr.weit.roadyfoody.user.fixture.createTestUser

@RepositoryTest
class UserRepositoryTest(
    private val userRepository: UserRepository,
) : DescribeSpec({

        lateinit var givenUser: User

        beforeEach {
            givenUser = userRepository.save(createTestUser())
        }

        describe("getByUserId 메소드는") {
            context("존재하는 id 를 받는 경우") {
                it("일치하는 유저를 반환한다.") {
                    val user = userRepository.getByUserId(givenUser.id)
                    user shouldBe givenUser
                }
            }

            context("존재하지 않는 id 를 받는 경우") {
                it("UserNotFoundException 을 던진다.") {
                    val ex =
                        shouldThrow<UserNotFoundException> {
                            userRepository.getByUserId(TEST_NONEXISTENT_ID)
                        }
                    ex.message shouldBe "$TEST_NONEXISTENT_ID ID 의 사용자는 존재하지 않습니다."
                }
            }
        }

        describe("getByNickname 메소드는") {
            context("존재하는 nickname 을 받는 경우") {
                it("일치하는 유저를 반환한다.") {
                    val user = userRepository.getByNickname(givenUser.profile.nickname)
                    user shouldBe givenUser
                }
            }

            context("존재하지 않는 nickname 을 받는 경우") {
                it("UserNotFoundException 을 던진다.") {
                    val ex =
                        shouldThrow<UserNotFoundException> {
                            userRepository.getByNickname(TEST_NONEXISTENT_NICKNAME)
                        }
                    ex.message shouldBe "$TEST_NONEXISTENT_NICKNAME 닉네임의 사용자는 존재하지 않습니다."
                }
            }
        }

        describe("getBySocialId 메소드는") {
            context("존재하는 socialId 를 받는 경우") {
                it("일치하는 유저를 반환한다.") {
                    val user = userRepository.getBySocialId(givenUser.socialId)
                    user shouldBe givenUser
                }
            }

            context("존재하지 않는 socialId 를 받는 경우") {
                it("UserNotFoundException 을 던진다.") {
                    val ex =
                        shouldThrow<UserNotFoundException> {
                            userRepository.getBySocialId(TEST_NONEXISTENT_NICKNAME)
                        }
                    ex.message shouldBe "$TEST_NONEXISTENT_NICKNAME 소셜 ID 의 사용자는 존재하지 않습니다."
                }
            }
        }

        describe("existsByProfileNickname 메소드는") {
            context("존재하는 nickname 을 받는 경우") {
                it("true 를 반환한다.") {
                    val exists = userRepository.existsByProfileNickname(givenUser.profile.nickname)
                    exists.shouldBeTrue()
                }
            }

            context("존재하지 않는 nickname 을 받는 경우") {
                it("false 를 반환한다.") {
                    val exists = userRepository.existsByProfileNickname(TEST_NONEXISTENT_NICKNAME)
                    exists.shouldBeFalse()
                }
            }
        }

        describe("existsBySocialId 메소드는") {
            context("존재하는 socialId 를 받는 경우") {
                it("true 를 반환한다.") {
                    val exists = userRepository.existsBySocialId(givenUser.socialId)
                    exists.shouldBeTrue()
                }
            }

            context("존재하지 않는 socialId 를 받는 경우") {
                it("false 를 반환한다.") {
                    val exists = userRepository.existsBySocialId(TEST_NONEXISTENT_NICKNAME)
                    exists.shouldBeFalse()
                }
            }
        }
    })
