package kr.weit.roadyfoody.term.fixture

import kr.weit.roadyfoody.term.domain.Term

const val TEST_TERM_PREFIX = "테스트 약관"

// Required Terms
fun createTestRequiredTerm(id: Long) =
    Term(
        id,
        "$TEST_TERM_PREFIX $id",
        coverContentHtmlTag("$TEST_TERM_PREFIX $id 내용"),
        true,
    )

const val TEST_REQUIRED_TERMS_SIZE = 3

fun createTestRequiredTerms() = (1L..TEST_REQUIRED_TERMS_SIZE).map { createTestRequiredTerm(it) }

fun createTestRequiredTermIdSet() = createTestRequiredTerms().map { it.id }.toSet()

// Optional Terms
fun createTestOptionalTerm(id: Long) =
    Term(
        id,
        "$TEST_TERM_PREFIX $id",
        coverContentHtmlTag("$TEST_TERM_PREFIX $id 내용"),
        false,
    )

const val TEST_OPTIONAL_TERMS_SIZE = 3

fun createTestOptionalTerms() =
    (TEST_REQUIRED_TERMS_SIZE + 1L..TEST_REQUIRED_TERMS_SIZE + TEST_OPTIONAL_TERMS_SIZE).map {
        createTestOptionalTerm(it)
    }

// All Terms
fun createTestTerms() = createTestRequiredTerms() + createTestOptionalTerms()

fun createTestTermIdSet() = createTestTerms().map { it.id }.toSet()

private fun coverContentHtmlTag(content: String) = """<!DOCTYPE html><html lang="ko">$content</html>"""

// Fail Case
const val TEST_NONEXISTENT_TERM_ID = 0L

fun createTestNotEnoughRequiredTermIdSet() = createTestRequiredTerms().dropLast(1).map { it.id }.toSet()
