package com.admin.base.utils;


import com.admin.base.config.security.TokenUser;
import com.admin.base.config.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/5/8 1:48 下午
 * @desc JWT token 工具类 — 已升级至 JJWT 0.12.6 API；使用 TokenUser DTO 序列化 claims
 */
@Slf4j
public class JwtTokenUtil implements Serializable {
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_DETAIL = "details";
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 从配置的 jwt.secret 派生签名密钥。
     * 优先尝试 Base64 解码（符合 JJWT 0.12 推荐的密钥格式），
     * 解码失败则回退为原始 UTF-8 字节。
     * JJWT 0.12 强制要求 HS256 密钥长度 ≥ 256 bits（32 字节）。
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(generateExpirationDate())
                .signWith(getSigningKey())
                .compact();
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.info("JWT格式验证失败:{}", token);
        }
        return claims;
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claimFromToken = getClaimsFromToken(token);
            username = claimFromToken.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String name = getUserNameFromToken(token);
        return name.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date fromToken = getExpiredDateFromToken(token);
        return fromToken.before(new Date());
    }

    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 根据 UserDetails 生成 JWT token。
     * 将轻量级 TokenUser DTO（而非完整的 UserDetailsImpl）序列化到 claims 中，
     * 确保 token payload 可被 Jackson 正常往返序列化/反序列化。
     */
    public String generateToken(UserDetails userDetails) {
        UserDetailsImpl impl = (UserDetailsImpl) userDetails;
        TokenUser tokenUser = TokenUser.from(impl);
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, tokenUser.username());
        claims.put(CLAIM_KEY_CREATED, new Date());
        try {
            claims.put(CLAIM_KEY_DETAIL, objectMapper.writeValueAsString(tokenUser));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize token user", e);
            throw new RuntimeException("Token generation failed", e);
        }
        return generateToken(claims);
    }

    public String refreshHeadToken(String oldToken) {
        if (StringUtils.isEmpty(oldToken)) {
            return null;
        }
        String token = oldToken.substring(tokenHead.length());
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        if (isTokenExpired(token)) {
            return null;
        }
        if (tokenRefreshJustBefore(token, 30 * 60)) {
            return token;
        } else {
            claims.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(claims);
        }
    }

    private boolean tokenRefreshJustBefore(String token, int time) {
        Claims claims = getClaimsFromToken(token);
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class);
        Date refreshDate = new Date();
        return refreshDate.after(created) && refreshDate.before(DateUtils.dateAddSeconds(created, time));
    }

    /**
     * 从 token claims 中重建 UserDetails。
     * 先反序列化轻量级 TokenUser DTO，再转换为 UserDetailsImpl，
     * 避免直接对 UserDetailsImpl 做 Jackson 反序列化（其含复杂嵌套类型，不可被 Jackson 构造）。
     */
    public UserDetails getUserDetail(String authToken) {
        Claims claims = getClaimsFromToken(authToken);
        if (claims == null) {
            return null;
        }
        try {
            TokenUser tokenUser = objectMapper.readValue(
                    claims.get(CLAIM_KEY_DETAIL).toString(), TokenUser.class);
            return tokenUser.toUserDetails();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize token user", e);
            return null;
        }
    }
}