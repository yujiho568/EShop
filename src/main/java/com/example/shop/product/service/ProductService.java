package com.example.shop.product.service;

import com.example.shop.comment.service.CommentService;
import com.example.shop.product.domain.Product;
import com.example.shop.product.domain.dto.ProductCreateRequest;
import com.example.shop.product.domain.dto.ProductListResponse;
import com.example.shop.product.domain.dto.ProductResponse;
import com.example.shop.product.domain.dto.ProductUpdateRequest;
import com.example.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CommentService commentService;

    public void saveProduct(ProductCreateRequest request) {
        Product product = Product.create(request.getTitle(), request.getPrice(), request.getImage());
        productRepository.save(product);
    }

    public void updateProduct(Integer id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        product.update(request.getTitle(), request.getPrice());
        productRepository.save(product);
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    public ModelAndView getDetailPage(Integer id) {
        Optional<ProductResponse> product = productRepository.findById(id).map(this::toResponse);
        if (product.isEmpty()) {
            return new ModelAndView("redirect:/list");
        }

        ModelAndView modelAndView = new ModelAndView("detail");
        modelAndView.addObject("product", product.get());
        modelAndView.addObject("comments", commentService.getComments(product.get().getId().longValue()));
        return modelAndView;
    }

    public ModelAndView getEditPage(Integer id) {
        Optional<ProductUpdateRequest> product = productRepository.findById(id)
                .map(savedProduct -> {
                    ProductUpdateRequest request = new ProductUpdateRequest();
                    request.setTitle(savedProduct.getTitle());
                    request.setPrice(savedProduct.getPrice());
                    return request;
                });

        if (product.isEmpty()) {
            return new ModelAndView("redirect:/list");
        }

        ModelAndView modelAndView = new ModelAndView("edit");
        modelAndView.addObject("id", id);
        modelAndView.addObject("product", product.get());
        return modelAndView;
    }

    public ModelAndView getListPageView(int pageNumber) {
        if (pageNumber < 1) {
            return new ModelAndView("redirect:/list/page/1");
        }

        ProductListResponse result = getListPage(pageNumber);
        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addObject("products", result.getProducts());
        modelAndView.addObject("currentPage", result.getCurrentPage());
        modelAndView.addObject("totalPages", result.getTotalPages());
        return modelAndView;
    }

    public ModelAndView getSearchPageView(String searchText, int pageNumber) {
        int safePage = Math.max(pageNumber, 1);
        ProductListResponse result = search(searchText, safePage);
        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addObject("products", result.getProducts());
        modelAndView.addObject("currentPage", result.getCurrentPage());
        modelAndView.addObject("totalPages", result.getTotalPages());
        modelAndView.addObject("searchText", result.getSearchText());
        return modelAndView;
    }

    private ProductListResponse getListPage(int pageNumber) {
        Page<Product> result = productRepository.findPageBy(PageRequest.of(pageNumber - 1, 5));
        return new ProductListResponse(toResponses(result.getContent()), pageNumber, result.getTotalPages(), null);
    }

    private ProductListResponse search(String searchText, int pageNumber) {
        Page<Product> result = productRepository.fullTextSearch(searchText, PageRequest.of(pageNumber - 1, 5));
        return new ProductListResponse(toResponses(result.getContent()), pageNumber, result.getTotalPages(), searchText);
    }

    private List<ProductResponse> toResponses(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getTitle(), product.getPrice(), product.getImage());
    }
}
