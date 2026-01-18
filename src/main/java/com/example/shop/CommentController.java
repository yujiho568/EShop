package com.example.shop;

import com.example.shop.item.ItemRepository;
import com.example.shop.member.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor

public class CommentController {
    private final CommentRepository commentRepository;
    @PostMapping("/comment")
    String postComment(@ModelAttribute Comment comment, Authentication auth) {
        if(!auth.isAuthenticated())
            return "redirect:/login";
        MyUserDetailsService.CustomUser user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
        comment.setUsername(user.getUsername());
        commentRepository.save(comment);
        return "redirect:/detail/" + comment.getParentId();
    }
}
