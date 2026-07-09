package com.admin.base.service.system.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.base.component.EntityInit;
import com.admin.base.constant.RedisPrefix;
import com.admin.base.constant.ResponseCode;
import com.admin.base.common.JsonResponse;
import com.admin.base.component.ResponseInit;
import com.admin.base.config.security.UserDetailsImpl;
import com.admin.base.config.security.UserDetailsServiceImpl;
import com.admin.base.dto.request.system.LoginParam;
import com.admin.base.dto.response.system.LoginResponse;
import com.admin.base.entity.system.Admin;
import com.admin.base.exception.BusinessException;
import com.admin.base.mapper.system.AdminMapper;
import com.admin.base.service.ICacheService;
import com.admin.base.service.system.IAdminRoleService;
import com.admin.base.service.system.IAdminService;
import com.admin.base.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private IAdminRoleService iAdminRoleService;
//    @Resource
//    @Lazy
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private ICacheService iCacheService;

    @Resource
    private ICacheService cacheService;


    @Override
    public Admin selectByUserName(String username) {
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.lambda().eq(Admin::getUserName, username);
        final Admin admin = this.baseMapper.selectOne(adminQueryWrapper);
        if (admin == null) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "不存在该管理员！"));
        }
        return admin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(String username, String password, String nickName, List<Integer> roleIds) {
        //检查用户名是否存在
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.lambda().eq(Admin::getUserName, username);
        final Long count = this.baseMapper.selectCount(adminQueryWrapper);
        if (count > 0) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "该账号已存在，请重新设置！"));
        }
        Admin admin = EntityInit.initAdmin(username, passwordEncoder.encode(password), password, nickName);
        Integer adminId = this.baseMapper.insertAdmin(admin);
        log.info("插入管理员成功：Id为" + admin.getAdminId());
        //添加管理员角色
        for (Integer roleId : roleIds) {
            //TODO 修改
//            final IAdminRoleService iAdminRoleService = SpringUtil.getBean("iAdminRoleService");
            iAdminRoleService.addAdminRole(admin.getAdminId(), roleId);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer adminId) {
        Admin admin = this.baseMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "账号不存在"));
        }
        //判断账号是否为自己
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getDetails();
        String username = userDetails.getUsername();
        Admin selectByUserName = selectByUserName(username);
        if (selectByUserName.getAdminId().equals(adminId)) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "不能删除自己"));
        }
        //删除
        this.baseMapper.deleteById(adminId);
        //删除对应的角色数据
        //TODO 修改
//        final IAdminRoleService iAdminRoleService = SpringUtil.getBean("iAdminRoleService");
        iAdminRoleService.updateAdminOfRole(adminId, new ArrayList<>());

    }

    @Override
    public void updatePassword(Integer adminId, String password) {
        UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(Admin::getAdminId, adminId)
                .set(Admin::getPassword, passwordEncoder.encode(password))
//                .set(Admin::getPasswordShow, password)
                .set(Admin::getUpdateTime, LocalDateTime.now());
        this.baseMapper.update(null, updateWrapper);
    }

    @Override
    public IPage<Admin> getAdminList(Integer page, Integer size, String name) {
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.lambda()
                .like(!StringUtils.isEmpty(name), Admin::getUserName, name);
        IPage<Admin> iPage = new Page<>(page, size);
        return this.baseMapper.selectPage(iPage, adminQueryWrapper);
    }

    @Override
    public LoginResponse login(LoginParam loginParam) {
        //校验验证码   可加全局配置  是否开启验证码
        validateCaptcha(loginParam.getUuid(), loginParam.getCode());
        //认证
//        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginParam.getUsername(), loginParam.getPassword()));
//        final UserDetailsImpl userDetails = (UserDetailsImpl) authenticate.getPrincipal();
        final UserDetailsServiceImpl userDetailsService = SpringUtil.getBean("userDetailsService");
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(loginParam.getUsername());
//        UserDetailsImpl userDetails = (UserDetailsImpl) this.userDetailsService.loadUserByUsername(loginParam.getUsername());
        log.debug(userDetails.getUsername() + "---------");
        if (!passwordEncoder.matches(loginParam.getPassword(), userDetails.getPassword())) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_SYS_ERROR, "用户名或密码错误"));
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenUtil.generateToken(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        cacheService.saveToken(token,userDetails.getAdminId().toString());
        //生成token
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
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_ALERT, "验证码失效，请重新获取！"));
        }
        if (!code.equals(captcha)) {
            throw new BusinessException(JsonResponse.error(ResponseCode.CODE_ALERT, "验证码错误！"));
        }
    }

    @Override
    public void updateAdminState(Integer adminId, Integer state) {
        UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(Admin::getAdminId, adminId)
                .set(Admin::getState, state)
                .set(Admin::getUpdateTime, LocalDateTime.now());
        this.baseMapper.update(null, updateWrapper);
    }

}
