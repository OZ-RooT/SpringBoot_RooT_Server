package io.github._3xhaust.root_server.domain.image.controller;

import io.github._3xhaust.root_server.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 업로드 및 조회 API")
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 업로드하고 id, url을 반환합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageUploadResponse response = imageService.saveImage(file);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Base64 이미지 업로드", description = "Base64 인코딩된 이미지를 업로드하고 id, url을 반환합니다.")
    @PostMapping("/upload/base64")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadBase64(@RequestParam("base64") String base64) throws IOException {
        ImageUploadResponse response = imageService.saveBase64Image(base64);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "이미지 조회", description = "저장된 이미지를 반환합니다.")
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        byte[] image = imageService.loadImage(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }
}
