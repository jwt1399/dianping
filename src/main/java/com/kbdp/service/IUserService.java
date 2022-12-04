package com.kbdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kbdp.dto.LoginFormDTO;
import com.kbdp.dto.Result;
import com.kbdp.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result logout(HttpServletRequest httpServletRequest);

    Result sign();

    Result signCount();
}
