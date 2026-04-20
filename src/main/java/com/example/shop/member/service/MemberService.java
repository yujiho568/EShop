package com.example.shop.member.service;

import com.example.shop.member.domain.Member;
import com.example.shop.member.domain.dto.MemberProfileResponse;
import com.example.shop.member.domain.dto.MemberRegisterRequest;
import com.example.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public ModelAndView registerMember(MemberRegisterRequest request, RedirectAttributes redirectAttributes) {
        try {
            String username = Objects.requireNonNullElse(request.getUsername(), "").trim();
            String password = Objects.requireNonNullElse(request.getPassword(), "").trim();
            String displayName = Objects.requireNonNullElse(request.getDisplayName(), "").trim();

            if (username.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
                throw new Exception("all fields are required");
            }
            if (memberRepository.findByUsername(username).isPresent()) {
                throw new Exception("already exist");
            }
            if (memberRepository.findByDisplayName(displayName).isPresent()) {
                throw new Exception("display name already exist");
            }
            if (username.length() < 4 || password.length() < 8) {
                throw new Exception("too short");
            }

            Member member = Member.create(username, passwordEncoder.encode(password), displayName);
            memberRepository.save(member);

            redirectAttributes.addFlashAttribute("successMessage", "Registration completed. Please log in.");
            return new ModelAndView("redirect:/login");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("registerError", e.getMessage());
            redirectAttributes.addFlashAttribute("member", request);
            return new ModelAndView("redirect:/register");
        }
    }

    public ModelAndView getRegisterPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return new ModelAndView("redirect:/list");
        }
        return new ModelAndView("register");
    }

    public ModelAndView getMyPage(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }

        MyUserDetailsService.CustomUser user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
        MemberProfileResponse profile = new MemberProfileResponse(user.getUsername(), user.displayName);
        ModelAndView modelAndView = new ModelAndView("mypage");
        modelAndView.addObject("displayName", profile.getDisplayName());
        return modelAndView;
    }
}
