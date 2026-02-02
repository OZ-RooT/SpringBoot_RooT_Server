package io.github._3xhaust.root_server.domain.translationcache.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github._3xhaust.root_server.domain.translationcache.entity.TranslationCache;
import io.github._3xhaust.root_server.domain.translationcache.repository.TranslationCacheRepository;
import io.github._3xhaust.root_server.infrastructure.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranslationService {

    private final TranslationCacheRepository translationCacheRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RedisCacheService redisCacheService;

    @Value("${google.translate.api-key:}")
    private String googleApiKey;

    private static final String CACHE_PREFIX_TRANSLATION = "translation:";
    private static final Duration CACHE_TTL = Duration.ofDays(30);

    /**
     * 텍스트를 대상 언어로 번역합니다.
     * 캐시에 있으면 캐시에서 반환하고, 없으면 Google Translate API를 호출합니다.
     */
    @Transactional
    public String translate(String text, String targetLanguage) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String hash = generateHash(text, targetLanguage);
        String cacheKey = CACHE_PREFIX_TRANSLATION + hash;

        Optional<String> redisCache = redisCacheService.get(cacheKey, String.class);
        if (redisCache.isPresent()) {
            log.debug("Translation Redis cache hit for hash: {}", hash);
            return redisCache.get();
        }

        Optional<TranslationCache> dbCache = translationCacheRepository.findByHash(hash);
        if (dbCache.isPresent()) {
            log.debug("Translation DB cache hit for hash: {}", hash);
            String translatedText = dbCache.get().getTranslatedText();
            redisCacheService.set(cacheKey, translatedText, CACHE_TTL);
            return translatedText;
        }

        String translatedText = callGoogleTranslateApi(text, targetLanguage);

        TranslationCache cache = TranslationCache.builder()
                .hash(hash)
                .targetLanguage(targetLanguage)
                .originText(text)
                .translatedText(translatedText)
                .build();
        translationCacheRepository.save(cache);
        redisCacheService.set(cacheKey, translatedText, CACHE_TTL);

        log.debug("Translation cached for hash: {}", hash);
        return translatedText;
    }

    /**
     * 원본 텍스트와 대상 언어를 조합하여 SHA-256 해시를 생성합니다.
     */
    private String generateHash(String text, String targetLanguage) {
        try {
            String combined = text + "|" + targetLanguage;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Google Translate API를 호출하여 번역을 수행합니다.
     */
    private String callGoogleTranslateApi(String text, String targetLanguage) {
        if (googleApiKey == null || googleApiKey.isBlank()) {
            log.warn("Google Translate API key is not configured. Returning original text.");
            return text;
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://translation.googleapis.com/language/translate/v2")
                    .queryParam("key", googleApiKey)
                    .queryParam("q", text)
                    .queryParam("target", targetLanguage)
                    .queryParam("format", "text")
                    .build()
                    .toUri();

            String response = restTemplate.postForObject(uri, null, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode translations = root.path("data").path("translations");

            if (!translations.isEmpty()) {
                return translations.get(0).path("translatedText").asText();
            }

            log.warn("No translation found in Google API response");
            return text;
        } catch (Exception e) {
            log.error("Failed to call Google Translate API: {}", e.getMessage());
            return text;
        }
    }

    /**
     * 캐시에서 번역을 조회합니다 (API 호출 없이).
     */
    public Optional<String> getCachedTranslation(String text, String targetLanguage) {
        String hash = generateHash(text, targetLanguage);
        return translationCacheRepository.findByHash(hash)
                .map(TranslationCache::getTranslatedText);
    }
}
