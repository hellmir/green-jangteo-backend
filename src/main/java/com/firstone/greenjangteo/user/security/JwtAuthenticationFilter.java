package com.firstone.greenjangteo.user.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String CRON_PATH_PREFIX = "/cron/";

    /**
     * Vercel Cron은 Authorization 헤더에 JWT가 아닌 CRON_SECRET을 담아 호출한다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(CRON_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal
            (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        log.info("Request IP Address: " + ipAddress);

        String token = resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info(String.format("[%s] -> %s", jwtTokenProvider.getUserId(token), request.getRequestURI()));
            } catch (UsernameNotFoundException e) {
                customAuthenticationEntryPoint.commence(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
