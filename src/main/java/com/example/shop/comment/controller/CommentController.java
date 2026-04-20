package com.example.shop.comment.controller;

import com.example.shop.comment.domain.dto.CommentCreateRequest;
import com.example.shop.comment.service.CommentService;
import com.example.shop.member.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/comment")
    String postComment(@ModelAttribute CommentCreateRequest request, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "redirect:/login";
        }

        MyUserDetailsService.CustomUser user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
        commentService.save(request, user.getUsername());
        return "redirect:/detail/" + request.getProductId();
    }
}
