package kr.weit.roadyfoody.global.config

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.QueryFactoryImpl
import com.linecorp.kotlinjdsl.query.creator.CriteriaQueryCreatorImpl
import com.linecorp.kotlinjdsl.query.creator.SubqueryCreatorImpl
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryFactoryConfig {
    @Bean
    fun queryFactory(entityManager: EntityManager): QueryFactory =
        QueryFactoryImpl(
            criteriaQueryCreator = CriteriaQueryCreatorImpl(entityManager),
            subqueryCreator = SubqueryCreatorImpl(),
        )
}
