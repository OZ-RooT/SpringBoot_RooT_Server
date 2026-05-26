package io.github._3xhaust.root_server.domain.image.controller;

import io.github._3xhaust.root_server.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 업로드 및 조회 API")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageUploadResponse response = imageService.saveImage(file);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        ImageUploadResponse response = imageService.saveVideo(file);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @PostMapping("/upload/base64")
    public ApiResponse<ImageUploadResponse> uploadBase64(@RequestParam("base64") String base64) throws IOException {
        ImageUploadResponse response = imageService.saveBase64Image(base64);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        byte[] image = imageService.loadImage(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imageService.getImageContentType(filename)));
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    @GetMapping("/videos/{filename:.+}")
    public ResponseEntity<ResourceRegion> getVideo(
            @PathVariable String filename,
            @RequestHeader HttpHeaders headers
    ) throws IOException {
        Resource video = imageService.loadVideo(filename);
        ResourceRegion region = getResourceRegion(video, headers);
        HttpStatusCode status = headers.getRange().isEmpty() ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(imageService.getVideoContentType(filename)))
                .body(region);
    }

    private ResourceRegion getResourceRegion(Resource video, HttpHeaders headers) throws IOException {
        long contentLength = video.contentLength();
        List<HttpRange> ranges = headers.getRange();
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        }
        return new ResourceRegion(video, 0, Math.min(1024 * 1024, contentLength));
    }
}
