package com.admin.base.filter;

import com.admin.base.config.security.UserDetailsImpl;
import com.admin.base.service.ICacheService;
import com.admin.base.constant.ResponseCode;
import com.admin.base.common.JsonResponse;
import com.admin.base.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/12 2:09 下午
 * @desc JWT authentication filter — replaced Gson with Jackson
 */
@Slf4j
public class MyTokenFilter extends GenericFilterBean {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private ICacheService cacheService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    private final ObjectMapper objectMapper = new ObjectMapper();

    HttpServletResponse setHead(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Access-Token");
        response.setHeader("Access-Control-Expose-Headers", "*");
        return response;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        String authHeader = request.getHeader(this.tokenHeader);
        log.debug("JWT登陆授权过滤器头信息{}", authHeader);
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // The part after "Bearer "
            String authToken = authHeader.substring(this.tokenHead.length());
            String username = jwtTokenUtil.getUserNameFromToken(authToken);
            if (username == null && !StringUtils.isEmpty(authToken)) {
                response = setHead(response);
                writeErrorResponse(response, ResponseCode.CODE_TOKEN_ERROR, "token失效,请重新登录");
                return;
            }
            log.debug("checking{} 是否重复登录", username);
            //Redis 中验证token 查询是否被挤掉

            UserDetailsImpl userDetail = (UserDetailsImpl) jwtTokenUtil.getUserDetail(authToken);
            // 再使用Redis 就违背了Jwt设计的思想
            if (StringUtils.isEmpty(cacheService.getTokenById(userDetail.getAdminId().toString())) || !cacheService.getTokenById(userDetail.getAdminId().toString()).equals(authToken)) {
                log.info("账号被挤掉");
                response = setHead(response);
                writeErrorResponse(response, ResponseCode.CODE_NO_LOGIN, "token失效,请重新登录");
                return;
            }

            log.debug("checking{} Jwt中token是否过期", username);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                //个人认为如果使用Redis进行单点登录的话，就不用jwt了，可以在登录的时候把 userDetails  放入redis
                UserDetails userDetails = jwtTokenUtil.getUserDetail(authToken);
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
                    authentication.setDetails(userDetails);
                    log.debug("authenticated user:{}", username);
                    //添加用户权限
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(objectMapper.writeValueAsString(JsonResponse.error(code, message)));
        response.getWriter().flush();
    }
}