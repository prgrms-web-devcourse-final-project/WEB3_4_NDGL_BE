package com.ndgl.spotfinder.domain;

import com.ndgl.spotfinder.global.security.cookie.TokenCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
public class HomeController {
    private final TokenCookieUtil tokenCookieUtil;

    @GetMapping("/api")
    String showMain() {
        return "Hello, NDGL!";
    }

    @GetMapping("/api/test-set-cookie")
    String testSetCookie(HttpServletResponse response) {
        String accessToken = "abc";
        tokenCookieUtil.setTokenCookies(response, accessToken);

        return "쿠키 : accessToken=%s".formatted(accessToken);
    }

    @GetMapping("/api/test-get-cookie")
    String testGetCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return "";

        return
                Arrays
                        .stream(request.getCookies())
                        .filter(cookie -> cookie.getName()
                                .equals("accessToken"))
                        .findFirst()
                        .get()
                        .getValue();
    }
}
