package kr.weit.roadyfoody.global.service

import kr.weit.roadyfoody.global.utils.MimeUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageService(
    private val storageService: StorageService,
) {
    fun upload(
        name: String,
        file: MultipartFile,
    ) {
        storageService.upload(name, file.inputStream)
    }

    fun downloadUrl(name: String): String = storageService.downloadUrl(name)

    fun generateImageName(file: MultipartFile): String {
        val extension = MimeUtils.getFileExtension(file)
        return "${UUID.randomUUID()}$extension"
    }
}
