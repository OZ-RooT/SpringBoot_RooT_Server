package io.github._3xhaust.root_server.domain.home.facade;

import io.github._3xhaust.root_server.domain.home.dto.res.*;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeFacade {

    private final ProductSearchRepository productSearchRepository;
    private final GarageSaleSearchRepository garageSaleSearchRepository;

    private static final short TYPE_USED = 0;
    private static final short TYPE_GARAGE = 1;

    /**
     * 홈 피드 조회
     * - 중고거래 상품 (type=0)
     * - 가라지 세일 목록
     * - 주변 가라지 세일 (위치 기반, 선택적)
     */
    public HomeFeedResponse getHomeFeed(int page, int limit, Double latitude, Double longitude, Double radiusKm) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 중고거래 상품 (type = 0)
        Page<ProductDocument> usedProductPage = productSearchRepository.findByTypeAndIsActiveTrue(TYPE_USED, pageable);
        List<HomeProductResponse> usedProducts = usedProductPage.getContent().stream()
                .map(HomeProductResponse::from)
                .toList();

        // 가라지 세일 목록
        Page<GarageSaleDocument> garageSalePage = garageSaleSearchRepository.findByIsActiveTrue(pageable);
        List<HomeGarageSaleResponse> garageSales = garageSalePage.getContent().stream()
                .map(HomeGarageSaleResponse::from)
                .toList();

        // 주변 가라지 세일 (위치 정보가 있는 경우)
        List<HomeGarageSaleResponse> nearbyGarageSales = null;
        if (latitude != null && longitude != null) {
            double radius = radiusKm != null ? radiusKm : 5.0; // 기본 5km
            Page<GarageSaleDocument> nearbyPage = garageSaleSearchRepository.findByLocationNear(
                    latitude, longitude, radius, PageRequest.of(0, 10));
            nearbyGarageSales = nearbyPage.getContent().stream()
                    .map(doc -> {
                        double distance = calculateDistance(latitude, longitude, doc.getLatitude(), doc.getLongitude());
                        return HomeGarageSaleResponse.from(doc, distance);
                    })
                    .toList();
        }

        return HomeFeedResponse.builder()
                .usedProducts(usedProducts)
                .garageSales(garageSales)
                .nearbyGarageSales(nearbyGarageSales)
                .pageInfo(HomeFeedResponse.PageInfo.builder()
                        .currentPage(page)
                        .totalPages(usedProductPage.getTotalPages())
                        .totalElements(usedProductPage.getTotalElements())
                        .hasNext(usedProductPage.hasNext())
                        .hasPrevious(usedProductPage.hasPrevious())
                        .build())
                .build();
    }

    /**
     * 중고거래 상품 목록 조회 (type=0)
     */
    public Page<HomeProductResponse> getUsedProducts(int page, int limit, String sortBy, String sortDir) {
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return productSearchRepository.findByTypeAndIsActiveTrue(TYPE_USED, pageable)
                .map(HomeProductResponse::from);
    }

    /**
     * 가라지 세일 목록 조회
     */
    public Page<HomeGarageSaleResponse> getGarageSales(int page, int limit, String sortBy, String sortDir) {
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return garageSaleSearchRepository.findByIsActiveTrue(pageable)
                .map(HomeGarageSaleResponse::from);
    }

    /**
     * 가라지 세일 상세 + 내부 상품 목록 (type=1)
     */
    public GarageSaleDetailResponse getGarageSaleDetail(Long garageSaleId, int productPage, int productLimit) {
        GarageSaleDocument garageSaleDoc = garageSaleSearchRepository
                .findById(GarageSaleDocument.generateId(garageSaleId))
                .orElseThrow(() -> new IllegalArgumentException("가라지 세일을 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(productPage - 1, productLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductDocument> productPage2 = productSearchRepository.findByGarageSaleIdAndIsActiveTrue(garageSaleId, pageable);

        return GarageSaleDetailResponse.builder()
                .garageSale(HomeGarageSaleResponse.from(garageSaleDoc))
                .products(productPage2.getContent().stream()
                        .map(HomeProductResponse::from)
                        .toList())
                .pageInfo(GarageSaleDetailResponse.PageInfo.builder()
                        .currentPage(productPage)
                        .totalPages(productPage2.getTotalPages())
                        .totalElements(productPage2.getTotalElements())
                        .hasNext(productPage2.hasNext())
                        .build())
                .build();
    }

    /**
     * 통합 검색 (상품 + 가라지 세일)
     */
    public SearchResponse search(String keyword, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProductDocument> productPage = productSearchRepository.searchByKeyword(keyword, pageable);
        Page<GarageSaleDocument> garageSalePage = garageSaleSearchRepository.searchByKeyword(keyword, pageable);

        return SearchResponse.builder()
                .keyword(keyword)
                .products(productPage.getContent().stream()
                        .map(HomeProductResponse::from)
                        .toList())
                .garageSales(garageSalePage.getContent().stream()
                        .map(HomeGarageSaleResponse::from)
                        .toList())
                .pageInfo(SearchResponse.SearchPageInfo.builder()
                        .productPage(page)
                        .productTotalPages(productPage.getTotalPages())
                        .productTotalElements(productPage.getTotalElements())
                        .garageSalePage(page)
                        .garageSaleTotalPages(garageSalePage.getTotalPages())
                        .garageSaleTotalElements(garageSalePage.getTotalElements())
                        .build())
                .build();
    }

    /**
     * 상품 검색 (중고거래만)
     */
    public Page<HomeProductResponse> searchUsedProducts(String keyword, int page, int limit,
                                                         Integer minPrice, Integer maxPrice) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProductDocument> result;
        if (minPrice != null && maxPrice != null) {
            result = productSearchRepository.findByPriceRangeAndType(minPrice, maxPrice, TYPE_USED, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = productSearchRepository.searchByKeywordAndType(keyword, TYPE_USED, pageable);
        } else {
            result = productSearchRepository.findByTypeAndIsActiveTrue(TYPE_USED, pageable);
        }

        return result.map(HomeProductResponse::from);
    }

    /**
     * 가라지 세일 검색 (위치 기반)
     */
    public Page<HomeGarageSaleResponse> searchNearbyGarageSales(Double latitude, Double longitude,
                                                                  Double radiusKm, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        double radius = radiusKm != null ? radiusKm : 10.0;

        return garageSaleSearchRepository.findByLocationNear(latitude, longitude, radius, pageable)
                .map(doc -> {
                    double distance = calculateDistance(latitude, longitude, doc.getLatitude(), doc.getLongitude());
                    return HomeGarageSaleResponse.from(doc, distance);
                });
    }

    /**
     * 현재 진행 중인 가라지 세일 조회
     */
    public Page<HomeGarageSaleResponse> getCurrentGarageSales(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "endTime"));
        return garageSaleSearchRepository.findCurrentlyActive(Instant.now(), pageable)
                .map(HomeGarageSaleResponse::from);
    }

    /**
     * 태그로 상품 검색
     */
    public Page<HomeProductResponse> getProductsByTag(String tag, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productSearchRepository.findByTagsContainingAndIsActiveTrue(tag, pageable)
                .map(HomeProductResponse::from);
    }

    /**
     * 태그로 가라지 세일 검색
     */
    public Page<HomeGarageSaleResponse> getGarageSalesByTag(String tag, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return garageSaleSearchRepository.findByTagsContainingAndIsActiveTrue(tag, pageable)
                .map(HomeGarageSaleResponse::from);
    }

    /**
     * 여러 태그로 상품 검색
     */
    public Page<HomeProductResponse> getProductsByTags(List<String> tags, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productSearchRepository.findByTagsInAndIsActiveTrue(tags, pageable)
                .map(HomeProductResponse::from);
    }

    /**
     * 기간 내 가라지 세일 조회
     */
    public Page<HomeGarageSaleResponse> getGarageSalesByDateRange(Instant startDate, Instant endDate,
                                                                    int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "startTime"));
        return garageSaleSearchRepository.findByDateRange(startDate, endDate, pageable)
                .map(HomeGarageSaleResponse::from);
    }

    // Haversine formula로 거리 계산 (km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반경 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Sort createSort(String sortBy, String sortDir) {
        String field = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}

