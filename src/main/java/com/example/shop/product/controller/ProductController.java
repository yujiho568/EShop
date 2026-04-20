package com.example.shop.product.controller;

import com.example.shop.global.service.S3Service;
import com.example.shop.product.domain.dto.ProductCreateRequest;
import com.example.shop.product.domain.dto.ProductUpdateRequest;
import com.example.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    @GetMapping("/list")
    String list() {
        return "redirect:/list/page/1";
    }

    @GetMapping("/write")
    String write() {
        return "write";
    }

    @PostMapping("/add")
    String addPost(@ModelAttribute ProductCreateRequest request) {
        productService.saveProduct(request);
        return "redirect:/list";
    }

    @PostMapping("/edit/{id}")
    String editPost(@PathVariable Integer id, @ModelAttribute ProductUpdateRequest request) {
        productService.updateProduct(id, request);
        return "redirect:/list";
    }

    @GetMapping("/detail/{id}")
    ModelAndView detail(@PathVariable Integer id) {
        return productService.getDetailPage(id);
    }

    @GetMapping("/edit/{id}")
    ModelAndView edit(@PathVariable Integer id) {
        return productService.getEditPage(id);
    }

    @DeleteMapping("/product")
    ResponseEntity<String> deleteProduct(@RequestParam Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(200).body("delete complete");
    }

    @GetMapping("/list/page/{id}")
    ModelAndView getListPage(@PathVariable Integer id) {
        return productService.getListPageView(id);
    }

    @GetMapping("/presigned-url")
    @ResponseBody
    String getURL(@RequestParam String filename) {
        return s3Service.createPresignedUrl("test/" + filename);
    }

    @GetMapping("/search")
    ModelAndView search(@RequestParam String searchText, @RequestParam(defaultValue = "1") int page) {
        return productService.getSearchPageView(searchText, page);
    }
}
