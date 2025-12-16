package io.github._3xhaust.root_server.domain.image.service;

import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.product.repository.ProductImageRepository;
import io.github._3xhaust.root_server.domain.product.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Transactional
    public ImageUploadResponse saveImage(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = now + "_" + UUID.randomUUID() + ext;
        File dest = new File(uploadDir, filename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest.getAbsoluteFile());
        Image image = Image.builder().url("/api/v1/images/" + filename).build();
        imageRepository.save(image);
        return new ImageUploadResponse(image.getId(), image.getUrl());
    }

    @Transactional
    public ImageUploadResponse saveBase64Image(String base64) throws IOException {
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = now + "_" + UUID.randomUUID() + ".jpg";
        File dest = new File(uploadDir, filename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(Base64.getDecoder().decode(base64));
        }
        Image image = Image.builder().url("/api/v1/images/" + filename).build();
        imageRepository.save(image);
        return new ImageUploadResponse(image.getId(), image.getUrl());
    }

    public byte[] loadImage(String filename) throws IOException {
        File file = new File(uploadDir, filename);
        return Files.readAllBytes(file.toPath());
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
}
