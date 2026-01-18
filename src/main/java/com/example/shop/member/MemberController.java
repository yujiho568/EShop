package com.example.shop.member;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MyUserDetailsService myUserDetailsService;
    @GetMapping("/login")
    public String login() {return "login";}

    @PostMapping("/register")
    String register(@ModelAttribute Member member) throws Exception {
        memberService.registerMember(member);
        return "redirect:/login";
    }

    @GetMapping("/register")
    String register(Authentication auth){
        if (auth.isAuthenticated()){
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
