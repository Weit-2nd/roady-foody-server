package kr.weit.roadyfoody.global.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.shouldForAny
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.weit.roadyfoody.support.utils.ImageFormat.GIF
import kr.weit.roadyfoody.support.utils.ImageFormat.JPEG
import kr.weit.roadyfoody.support.utils.ImageFormat.PNG
import kr.weit.roadyfoody.support.utils.ImageFormat.WEBP
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.user.fixture.TEST_USER_PROFILE_IMAGE_NAME
import java.io.InputStream

class ImageServiceTest : BehaviorSpec({
    val storageService = mockk<StorageService>()
    val imageService = ImageService(storageService)

    given("upload 테스트") {
        `when`("이미지를 업로드 하면") {
            every { storageService.upload(TEST_USER_PROFILE_IMAGE_NAME, any<InputStream>()) } returns ""
            then("이미지가 저장된다.") {
                imageService.upload(TEST_USER_PROFILE_IMAGE_NAME, createTestImageFile(WEBP))
                verify(exactly = 1) { storageService.upload(TEST_USER_PROFILE_IMAGE_NAME, any<InputStream>()) }
            }
        }
    }

    given("generateImageName 테스트") {
        `when`("WEBP 이미지 파일을 전달하면") {
            then("WEBP 확장자가 있는 이름이 생성된다.") {
                val imageName = imageService.generateImageName(createTestImageFile(WEBP))
                WEBP.values.shouldForAny { imageName.shouldEndWith(".$it") }
            }
        }

        `when`("JPEG 이미지 파일을 전달하면") {
            then("JPEG 확장자가 있는 이름이 생성된다.") {
                val imageName = imageService.generateImageName(createTestImageFile(JPEG))
                JPEG.values.shouldForAny { imageName.shouldEndWith(".$it") }
            }
        }

        `when`("PNG 이미지 파일을 전달하면") {
            then("PNG 확장자가 있는 이름이 생성된다.") {
                val imageName = imageService.generateImageName(createTestImageFile(PNG))
                PNG.values.shouldForAny { imageName.shouldEndWith(".$it") }
            }
        }

        `when`("GIF 이미지 파일을 전달하면") {
            then("GIF 확장자가 있는 이름이 생성된다.") {
                val imageName = imageService.generateImageName(createTestImageFile(GIF))
                GIF.values.shouldForAny { imageName.shouldEndWith(".$it") }
            }
        }
    }

    given("remove 테스트") {
        `when`("이미지를 제거하면") {
            every { storageService.delete(TEST_USER_PROFILE_IMAGE_NAME) } just runs
            then("이미지가 삭제된다.") {
                imageService.remove(TEST_USER_PROFILE_IMAGE_NAME)
                verify(exactly = 1) { storageService.delete(TEST_USER_PROFILE_IMAGE_NAME) }
            }
        }
    }
})
