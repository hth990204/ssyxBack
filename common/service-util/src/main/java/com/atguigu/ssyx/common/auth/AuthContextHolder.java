package com.atguigu.ssyx.common.auth;

import com.atguigu.ssyx.vo.user.UserLoginVo;

// ThreadLocal工具类
public class AuthContextHolder {

    // 用户Id
    private static  ThreadLocal<Long> userId = new ThreadLocal<>();

    // 用户仓库Id
    private static ThreadLocal<Long> wareId = new ThreadLocal<>();


    // 用户信息对象
    private static ThreadLocal<UserLoginVo> userLoginVo = new ThreadLocal<>();

    //userId操作方法
    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }

    public static Long getUserId() {
        return userId.get();
    }

    //wareId操作方法
    public static void setWareId(Long _wareId) {
        wareId.set(_wareId);
    }

    public static Long getWareId() {
        return wareId.get();
    }

    // userLoginVo操作方法
    public static UserLoginVo getUserLoginVo() {
        return userLoginVo.get();
    }

    public static void setUserLoginVo(UserLoginVo _userLoginVo) {
        userLoginVo.set(_userLoginVo);
    }
}
