package kr.weit.roadyfoody.review.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.roadyfoody.common.domain.BaseModifiableEntity
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.RoadyFoodyBadRequestException
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.user.domain.User

@Entity
@Table(name = "food_spots_reviews")
@SequenceGenerator(name = "FOOD_SPOTS_REVIEWS_SEQ_GENERATOR", sequenceName = "FOOD_SPOTS_REVIEWS_SEQ", initialValue = 1, allocationSize = 1)
class FoodSpotsReview(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FOOD_SPOTS_REVIEWS_SEQ_GENERATOR")
    val id: Long = 0L,
    @ManyToOne
    @JoinColumn(name = "food_spot_id", updatable = false)
    val foodSpots: FoodSpots,
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    val user: User,
    @Column(nullable = false)
    var rate: Int,
    @Column(nullable = false, length = 1200)
    var contents: String,
    @Column(nullable = false)
    var likeTotal: Int,
) : BaseModifiableEntity() {
    fun increaseLike() {
        this.likeTotal++
    }

    fun decreaseLike() {
        require(this.likeTotal > 0) { throw RoadyFoodyBadRequestException(ErrorCode.NEGATIVE_NUMBER_OF_LIKED) }
        this.likeTotal--
    }
}
