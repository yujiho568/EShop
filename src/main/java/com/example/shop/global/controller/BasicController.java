package com.example.shop.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
public class BasicController {
    @GetMapping("/")
    String hello() {
        return "redirect:/list/page/1";
    }

    @GetMapping("/date")
    @ResponseBody
    String date() {
        return LocalDateTime.now().toString();
    }
}
