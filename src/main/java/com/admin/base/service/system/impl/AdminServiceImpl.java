package com.admin.base.service.system.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.admin.base.common.PageResult;
import com.admin.base.component.EntityInit;
import com.admin.base.component.ResponseInit;
import com.admin.base.config.security.UserDetailsImpl;
import com.admin.base.config.security.UserDetailsServiceImpl;
import com.admin.base.constant.RedisPrefix;
import com.admin.base.constant.ResponseCode;
import com.admin.base.dto.request.system.LoginParam;
import com.admin.base.dto.response.system.LoginResponse;
import com.admin.base.entity.system.Admin;
import com.admin.base.exception.BusinessException;
import com.admin.base.repository.system.AdminRepository;
import com.admin.base.service.ICacheService;
import com.admin.base.service.system.IAdminRoleService;
import com.admin.base.service.system.IAdminService;
import com.admin.base.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final PasswordEncoder passwordEncoder;
    private final IAdminRoleService iAdminRoleService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ICacheService iCacheService;
    private final AdminRepository adminRepository;

    @Override
    public Admin selectByUserName(String username) {
        return adminRepository.findByUserName(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "不存在该管理员！"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(String username, String password, String nickName, List<Integer> roleIds) {
        if (adminRepository.existsByUserName(username)) {
            throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "该账号已存在，请重新设置！");
        }
        Admin admin = EntityInit.initAdmin(username, passwordEncoder.encode(password), password, nickName);
        Admin saved = adminRepository.save(admin);
        log.info("插入管理员成功：Id为{}", saved.getAdminId());
        for (Integer roleId : roleIds) {
            iAdminRoleService.addAdminRole(saved.getAdminId(), roleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer adminId) {
        Long adminIdValue = toLongId(adminId);
        Admin admin = adminRepository.findById(adminIdValue)
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "账号不存在"));
        String username = currentUsername();
        Admin currentAdmin = selectByUserName(username);
        if (currentAdmin.getAdminId().equals(admin.getAdminId())) {
            throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "不能删除自己");
        }
        adminRepository.deleteById(adminIdValue);
        iAdminRoleService.updateAdminOfRole(adminId, List.of());
    }

    @Override
    public void updatePassword(Integer adminId, String password) {
        Admin admin = adminRepository.findById(toLongId(adminId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "账号不存在"));
        admin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(admin);
    }

    @Override
    public PageResult<Admin> getAdminList(Integer page, Integer size, String name) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Admin> pageResult;
        if (StringUtils.hasText(name)) {
            pageResult = adminRepository.findByUserNameContaining(name, pageable);
        } else {
            pageResult = adminRepository.findAll(pageable);
        }
        return new PageResult<>(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    @Override
    public LoginResponse login(LoginParam loginParam) {
        validateCaptcha(loginParam.getUuid(), loginParam.getCode());
        UserDetailsServiceImpl userDetailsService = SpringUtil.getBean("userDetailsService");
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(loginParam.getUsername());
        log.debug("{}---------", userDetails.getUsername());
        if (!passwordEncoder.matches(loginParam.getPassword(), userDetails.getPassword())) {
            throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "用户名或密码错误");
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenUtil.generateToken(userDetails);

        iCacheService.saveToken(token, userDetails.getAdminId().toString());
        return ResponseInit.initLoginResponse(
                userDetails.getAdminId(),
                userDetails.getUsername(),
                userDetails.getNickName(),
                token);
    }

    private void validateCaptcha(String uuid, String code) {
        String verifyKey = RedisPrefix.CAPTCHA_CODE_KEY + uuid;
        String captcha = iCacheService.getValueByKey(verifyKey);
        if (StringUtils.isEmpty(captcha)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "验证码失效，请重新获取！");
        }
        if (!code.equals(captcha)) {
            throw new BusinessException(ResponseCode.CODE_ALERT, "验证码错误！");
        }
    }

    @Override
    public void updateAdminState(Integer adminId, Integer state) {
        Admin admin = adminRepository.findById(toLongId(adminId))
                .orElseThrow(() -> new BusinessException(ResponseCode.CODE_SYS_ERROR, "账号不存在"));
        admin.setState(state);
        adminRepository.save(admin);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object details = authentication == null ? null : authentication.getDetails();
        if (details instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new BusinessException(ResponseCode.CODE_NO_LOGIN, "获取用户账户异常");
    }

    private Long toLongId(Integer id) {
        return id == null ? null : id.longValue();
    }
}
