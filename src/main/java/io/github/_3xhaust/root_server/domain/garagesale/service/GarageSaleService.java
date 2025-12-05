package io.github._3xhaust.root_server.domain.garagesale.service;

import io.github._3xhaust.root_server.domain.garagesale.dto.req.CreateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.req.UpdateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleResponse;
import io.github._3xhaust.root_server.domain.garagesale.entity.FavoriteGarageSale;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleErrorCode;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleException;
import io.github._3xhaust.root_server.domain.garagesale.repository.FavoriteGarageSaleRepository;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleRepository;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItem;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.exception.ProductErrorCode;
import io.github._3xhaust.root_server.domain.product.exception.ProductException;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarageSaleService {

    private final GarageSaleRepository garageSaleRepository;
    private final FavoriteGarageSaleRepository favoriteGarageSaleRepository;
    private final ProductRepository productRepository;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final UserRepository userRepository;

    public List<GarageSaleListResponse> getAllGarageSales(Double lat, Double lng, Double radius) {
        List<GarageSale> garageSales;

        if (lat != null && lng != null && radius != null) {
            garageSales = garageSaleRepository.findNearbyGarageSales(lat, lng, radius);
        } else {
            garageSales = garageSaleRepository.findAll();
        }

        return garageSales.stream()
                .map(GarageSaleListResponse::of)
                .toList();
    }

    public GarageSaleResponse getGarageSaleById(Long id) {
        GarageSale garageSale = garageSaleRepository.findById(id)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + id));
        return GarageSaleResponse.of(garageSale);
    }

    @Transactional
    public GarageSaleResponse createGarageSale(String email, CreateGarageSaleRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        GarageSale garageSale = GarageSale.builder()
                .owner(owner)
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        GarageSale savedGarageSale = garageSaleRepository.save(garageSale);
        return GarageSaleResponse.ofWithoutProducts(savedGarageSale);
    }

    @Transactional
    public GarageSaleResponse updateGarageSale(String email, Long id, UpdateGarageSaleRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        GarageSale garageSale = garageSaleRepository.findById(id)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + id));

        if (!garageSale.getOwner().getId().equals(user.getId())) {
            throw new GarageSaleException(GarageSaleErrorCode.UNAUTHORIZED_ACCESS, "garageSaleId=" + id);
        }

        garageSale.update(
                request.getName() != null ? request.getName() : garageSale.getName(),
                request.getLatitude() != null ? request.getLatitude() : garageSale.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : garageSale.getLongitude(),
                request.getStartTime() != null ? request.getStartTime() : garageSale.getStartTime(),
                request.getEndTime() != null ? request.getEndTime() : garageSale.getEndTime()
        );

        return GarageSaleResponse.of(garageSale);
    }

    @Transactional
    public void toggleFavoriteGarageSale(String email, Long garageSaleId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        if (favoriteGarageSaleRepository.existsByUserIdAndGarageSaleId(user.getId(), garageSaleId)) {
            favoriteGarageSaleRepository.deleteByUserIdAndGarageSaleId(user.getId(), garageSaleId);
        } else {
            FavoriteGarageSale favorite = FavoriteGarageSale.builder()
                    .user(user)
                    .garageSale(garageSale)
                    .build();
            favoriteGarageSaleRepository.save(favorite);
        }
    }

    public ProductResponse getGarageSaleProduct(Long garageSaleId, Long productId) {
        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (product.getGarageSale() == null || !product.getGarageSale().getId().equals(garageSaleId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND,
                    "Product " + productId + " not found in garage sale " + garageSaleId);
        }

        return ProductResponse.of(product);
    }

    @Transactional
    public void toggleFavoriteGarageSaleProduct(String email, Long garageSaleId, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (product.getGarageSale() == null || !product.getGarageSale().getId().equals(garageSaleId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND,
                    "Product " + productId + " not found in garage sale " + garageSaleId);
        }

        if (favoriteUsedItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            favoriteUsedItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
        } else {
            FavoriteUsedItem favorite = FavoriteUsedItem.builder()
                    .user(user)
                    .product(product)
                    .build();
            favoriteUsedItemRepository.save(favorite);
        }
    }

    public List<GarageSaleListResponse> getFavoriteGarageSales(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        List<GarageSale> garageSales = favoriteGarageSaleRepository.findGarageSalesByUserId(user.getId());
        return garageSales.stream()
                .map(GarageSaleListResponse::of)
                .toList();
    }
}

