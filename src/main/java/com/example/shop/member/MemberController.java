package com.example.shop.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MyUserDetailsService myUserDetailsService;
    @GetMapping("/login")
    public String login() {return "login";}

    @PostMapping("/register")
    String register(@ModelAttribute Member member, RedirectAttributes redirectAttributes) {
        try {
            memberService.registerMember(member);
            redirectAttributes.addFlashAttribute("successMessage", "Registration completed. Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("registerError", e.getMessage());
            redirectAttributes.addFlashAttribute("member", member);
            return "redirect:/register";
        }
    }

    @GetMapping("/register")
    String register(Authentication auth){
        if (auth != null && auth.isAuthenticated()){
            return "redirect:/list";
        } return "register";
    }

    @GetMapping("/my-page")
    String myPage(Authentication auth, Model model) {
        MyUserDetailsService.CustomUser result = (MyUserDetailsService.CustomUser)auth.getPrincipal();
        System.out.println(result.displayName);
        model.addAttribute("displayName", result.displayName);
        if(auth.isAuthenticated())
            return "mypage";
        else return "redirect:/login";
    }
}
