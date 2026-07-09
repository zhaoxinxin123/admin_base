package com.admin.base.filter;

import com.admin.base.config.security.UserDetailsImpl;
import com.admin.base.service.ICacheService;
import com.admin.base.constant.ResponseCode;
import com.admin.base.common.JsonResponse;
import com.admin.base.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/12 2:09 下午
 * @desc JWT 认证过滤器 — OncePerRequestFilter 风格，使用 Jackson 写错误响应
 */
@Slf4j
public class MyTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private ICacheService cacheService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(this.tokenHeader);
        log.debug("JWT登陆授权过滤器头信息{}", authHeader);
        if (authHeader == null || !authHeader.startsWith(this.tokenHead)) {
            filterChain.doFilter(request, response);
            return;
        }

        // The part after "Bearer "
        String authToken = authHeader.substring(this.tokenHead.length()).trim();
        String username = jwtTokenUtil.getUserNameFromToken(authToken);
        if (username == null) {
            writeError(response, ResponseCode.CODE_TOKEN_ERROR, "token失效,请重新登录");
            return;
        }

        log.debug("checking{} 是否重复登录", username);
        // Redis 中验证 token，查询是否被挤掉
        UserDetailsImpl userDetail = (UserDetailsImpl) jwtTokenUtil.getUserDetail(authToken);
        if (userDetail == null) {
            log.info("token解析失败，userDetail为null");
            writeError(response, ResponseCode.CODE_TOKEN_ERROR, "token解析失败");
            return;
        }
        String cachedToken = cacheService.getTokenById(userDetail.getAdminId().toString());
        if (StringUtils.isBlank(cachedToken) || !cachedToken.equals(authToken)) {
            log.info("账号被挤掉");
            writeError(response, ResponseCode.CODE_NO_LOGIN, "token失效,请重新登录");
            return;
        }

        log.debug("checking{} Jwt中token是否过期", username);
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtTokenUtil.getUserDetail(authToken);
            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
                authentication.setDetails(userDetails);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("authenticated user:{}", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 使用 Jackson ObjectMapper 写入 JSON 错误响应，
     * 结构与 JsonResponse 统一，与 InvalidAuthenticationEntryPoint 的 401 语义一致。
     */
    private void writeError(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().println(objectMapper.writeValueAsString(JsonResponse.error(code, message)));
        response.getWriter().flush();
    }
}