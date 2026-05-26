package io.github._3xhaust.root_server.domain.image.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.product.repository.ProductImageRepository;
import io.github._3xhaust.root_server.domain.product.entity.ProductImage;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostImageRepository;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostImage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.Base64;
import java.util.UUID;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;
    private final CommunityPostImageRepository communityPostImageRepository;

    @Value("${image.upload.dir}")
    private String uploadDir;

    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "m4v", "webm", "avi", "mkv");

    @Transactional
    public ImageUploadResponse saveImage(MultipartFile file) throws IOException {
        String now = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String filename = now + "_" + UUID.randomUUID() + ".webp";
        File dest = new File(uploadDir, filename);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());
        WebpWriter writer = WebpWriter.DEFAULT.withQ(90);
        image.output(writer, dest);

        Image savedImage = Image.builder()
                .url("/api/v1/images/" + filename)
                .build();
        imageRepository.save(savedImage);

        return new ImageUploadResponse(savedImage.getId(), savedImage.getUrl());
    }

    @Transactional
    public ImageUploadResponse saveBase64Image(String base64) throws IOException {
        String now = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String filename = now + "_" + UUID.randomUUID() + ".webp";
        File dest = new File(uploadDir, filename);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        ImmutableImage image = ImmutableImage.loader().fromBytes(decodedBytes);
        WebpWriter writer = WebpWriter.DEFAULT.withQ(90);
        image.output(writer, dest);

        Image savedImage = Image.builder()
                .url("/api/v1/images/" + filename)
                .build();
        imageRepository.save(savedImage);

        return new ImageUploadResponse(savedImage.getId(), savedImage.getUrl());
    }

    @Transactional
    public ImageUploadResponse saveVideo(MultipartFile file) throws IOException {
        String extension = resolveVideoExtension(file);
        String now = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String filename = now + "_" + UUID.randomUUID() + "." + extension;
        File videoDir = getVideoUploadDir();
        File dest = new File(videoDir, filename);

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        file.transferTo(dest.toPath());

        Image savedVideo = Image.builder()
                .url("/api/v1/images/videos/" + filename)
                .build();
        imageRepository.save(savedVideo);

        return new ImageUploadResponse(savedVideo.getId(), savedVideo.getUrl());
    }

    public byte[] loadImage(String filename) throws IOException {
        File file = new File(uploadDir, filename);
        return Files.readAllBytes(file.toPath());
    }

    public String getImageContentType(String filename) throws IOException {
        File file = new File(uploadDir, filename);
        String contentType = Files.probeContentType(file.toPath());
        return contentType != null ? contentType : "image/jpeg";
    }

    public Resource loadVideo(String filename) throws IOException {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IOException("Invalid video filename");
        }
        File file = new File(getVideoUploadDir(), filename);
        if (!file.exists()) {
            throw new IOException("Video not found: " + filename);
        }
        return new UrlResource(file.toURI());
    }

    public String getVideoContentType(String filename) throws IOException {
        File file = new File(getVideoUploadDir(), filename);
        String contentType = Files.probeContentType(file.toPath());
        if (contentType != null) {
            return contentType;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".mov") || lower.endsWith(".m4v")) return "video/quicktime";
        if (lower.endsWith(".webm")) return "video/webm";
        return "video/mp4";
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteUnusedImages() {
        var allImages = imageRepository.findAll();
        var usedImageIds = userRepository.findAll().stream()
                .map(User::getProfileImage)
                .filter(java.util.Objects::nonNull)
                .map(Image::getId)
                .collect(java.util.stream.Collectors.toSet());
        usedImageIds.addAll(productImageRepository.findAll().stream()
                .map(ProductImage::getImage)
                .filter(java.util.Objects::nonNull)
                .map(Image::getId)
                .toList());
        usedImageIds.addAll(communityPostImageRepository.findAll().stream()
                .map(CommunityPostImage::getImage)
                .filter(java.util.Objects::nonNull)
                .map(Image::getId)
                .toList());

        for (Image image : allImages) {
            if (!usedImageIds.contains(image.getId())) {
                String url = image.getUrl();
                String filename = url.substring(url.lastIndexOf("/") + 1);
                File file = new File(uploadDir, filename);
                if (file.exists()) {
                    file.delete();
                }
                imageRepository.delete(image);
            }
        }
    }

    private File getVideoUploadDir() {
        File imageDir = new File(uploadDir);
        File parent = imageDir.getParentFile();
        return parent == null ? new File("uploads/videos") : new File(parent, "videos");
    }

    private String resolveVideoExtension(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = null;
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        if (extension == null || !VIDEO_EXTENSIONS.contains(extension)) {
            if ("video/quicktime".equals(contentType)) {
                extension = "mov";
            } else if ("video/webm".equals(contentType)) {
                extension = "webm";
            } else if (contentType != null && contentType.startsWith("video/")) {
                extension = "mp4";
            }
        }
        if (extension == null || !VIDEO_EXTENSIONS.contains(extension)) {
            throw new IOException("Unsupported video file");
        }
        return extension;
    }
}
