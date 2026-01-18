package com.example.shop.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    public void registerMember(Member member) throws Exception {
        if(memberRepository.findByUsername(member.getUsername()).isPresent()){
            throw new Exception("already exist");
        }
        if(member.getUsername().length() < 8 || member.getPassword().length() < 8)
            throw new Exception("too short");
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }


}
