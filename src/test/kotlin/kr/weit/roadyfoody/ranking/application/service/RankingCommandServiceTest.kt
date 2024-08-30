package kr.weit.roadyfoody.ranking.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.weit.roadyfoody.foodSpots.fixture.createUserRankingResponse
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsHistoryRepository
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.util.concurrent.TimeUnit

class RankingCommandServiceTest :
    BehaviorSpec(
        {
            val redisTemplate = mockk<RedisTemplate<String, String>>()
            val redissonClient = mockk<RedissonClient>()
            val foodSpotsHistoryRepository = mockk<FoodSpotsHistoryRepository>()
            val rankingCommandService =
                RankingCommandService(
                    redisTemplate,
                    redissonClient,
                    foodSpotsHistoryRepository,
                )

            given("updateReportRanking 테스트") {
                val mockLock = mockk<RLock>()
                val zSetOperations = mockk<ZSetOperations<String, String>>()
                val mockTypedTuple: Set<ZSetOperations.TypedTuple<String>> =
                    setOf(
                        mockk<ZSetOperations.TypedTuple<String>>().apply {
                            every { value } returns "user1"
                            every { score } returns 10.0
                        },
                        mockk<ZSetOperations.TypedTuple<String>>().apply {
                            every { value } returns "user2"
                            every { score } returns 5.0
                        },
                    )

                every { redissonClient.getLock(any<String>()) } returns mockLock

                `when`("Lock을 획득한 경우") {
                    every { mockLock.tryLock(0, 10, TimeUnit.MINUTES) } returns true

                    every { redisTemplate.delete("rofo:user-report-ranking") } returns true
                    every { foodSpotsHistoryRepository.findAllUserReportCount() } returns createUserRankingResponse()
                    every { redisTemplate.opsForZSet() } returns zSetOperations
                    every { zSetOperations.reverseRangeWithScores(any(), any(), any()) } returns mockTypedTuple
                    every { zSetOperations.add("rofo:user-report-ranking", "existentNick", 10.0) } returns true

                    then("레디스의 데이터가 정상적으로 업데이트된다.") {
                        rankingCommandService.updateReportRanking()
                        verify(exactly = 1) { foodSpotsHistoryRepository.findAllUserReportCount() }
                    }
                }

                `when`("Lock을 획득하지 못한 경우") {
                    every { mockLock.tryLock(0, 10, TimeUnit.MINUTES) } returns false

                    then("레디스의 데이터가 업데이트되지 않는다.") {
                        rankingCommandService.updateReportRanking()
                        verify(exactly = 1) { foodSpotsHistoryRepository.findAllUserReportCount() }
                    }
                }
            }
        },
    )
