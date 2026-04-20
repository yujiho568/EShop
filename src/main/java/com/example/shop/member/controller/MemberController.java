package com.example.shop.member.controller;

import com.example.shop.member.domain.dto.MemberRegisterRequest;
import com.example.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/register")
    ModelAndView register(@ModelAttribute MemberRegisterRequest request, RedirectAttributes redirectAttributes) {
        return memberService.registerMember(request, redirectAttributes);
    }

    @GetMapping("/register")
    ModelAndView register(Authentication auth) {
        return memberService.getRegisterPage(auth);
    }

    @GetMapping("/my-page")
    ModelAndView myPage(Authentication auth) {
        return memberService.getMyPage(auth);
    }
}
