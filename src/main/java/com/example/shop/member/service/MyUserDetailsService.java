package com.example.shop.member.service;

import com.example.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var result = memberRepository.findByUsername(username);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }

        var user = result.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("general"));
        CustomUser customUser = new CustomUser(user.getUsername(), user.getPassword(), authorities);
        customUser.id = user.getId();
        customUser.displayName = user.getDisplayName();
        return customUser;
    }

    public static class CustomUser extends User {
        public Long id;
        public String displayName;

        public CustomUser(String username, String password, List<GrantedAuthority> authorities) {
            super(username, password, authorities);
        }
    }
}
