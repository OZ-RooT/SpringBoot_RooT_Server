package io.github._3xhaust.root_server.domain.translationcache.controller;

import io.github._3xhaust.root_server.domain.translationcache.dto.req.TranslationRequest;
import io.github._3xhaust.root_server.domain.translationcache.dto.res.TranslationResponse;
import io.github._3xhaust.root_server.domain.translationcache.service.TranslationService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ApiResponse<TranslationResponse> translate(@RequestBody @Valid TranslationRequest request) {
        Optional<String> cached = translationService.getCachedTranslation(
                request.getText(),
                request.getTargetLanguage()
        );

        boolean isCached = cached.isPresent();
        String translatedText = isCached
                ? cached.get()
                : translationService.translate(request.getText(), request.getTargetLanguage());

        TranslationResponse response = TranslationResponse.builder()
                .originalText(request.getText())
                .translatedText(translatedText)
                .targetLanguage(request.getTargetLanguage())
                .cached(isCached)
                .build();

        return ApiResponse.ok(response);
    }
}

