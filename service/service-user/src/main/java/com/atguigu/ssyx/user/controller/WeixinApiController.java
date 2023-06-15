package com.atguigu.ssyx.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.execption.SsyxException;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.common.utils.JwtHelper;
import com.atguigu.ssyx.enums.UserType;
import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.user.service.UserService;
import com.atguigu.ssyx.user.utils.ConstantPropertiesUtil;
import com.atguigu.ssyx.user.utils.HttpClientUtils;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "微信登录获取openid(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result loginWx(@PathVariable String code) {

        // code + 小程序id + 小程序密钥 请求微信接口服务

        // 小程序id
        String wxOpenAppId = ConstantPropertiesUtil.WX_OPEN_APP_ID;

        // 密钥
        String wxOpenAppSecret = ConstantPropertiesUtil.WX_OPEN_APP_SECRET;

        //get 请求
        // 拼接请求地址 + 参数
        // 地址?name-value&name1=value1
        StringBuffer url = new StringBuffer().append("https://api.weixin.qq.com/sns/jscode2session").append("?appid=%s").append("&secret=%s").append("&js_code=%s").append("&grant_type=authorization_code");

        String tokenUrl = String.format(url.toString(), wxOpenAppId, wxOpenAppSecret, code);

        //HttpClient发送get请求
        String result = null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new SsyxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        // 返回两个值 session_key 和openid
        JSONObject jsonObject = JSONObject.parseObject(result);
//        String session_key = jsonObject.getString("session_key");
//        String openid = jsonObject.getString("openid");

//        TODO 为了测试，openId写固定
        String session_key = "";
        String openid = "odo3j4uGJf6Hl2FopkEOLGxr7LE4";



        // 是否第一次登录
        User user = userService.getUserByOpenId(openid);
        if (user == null) {
            user = new User();
            user.setOpenId(openid);
            user.setNickName(openid);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }

        // 根据userId查询提货点和团长信息
        // 提货点  user表 user_delivery表
        // 团长 leader表
        LeaderAddressVo leaderAddressVo =
                userService.getLeaderAddressByUserId(user.getId());

        // 使用JWT根据userId和userName生成token
        String token = JwtHelper.createToken(user.getId(), user.getNickName());

        // 获取用户信息，放到redis里
        UserLoginVo userLoginVo = userService.getUserLoginVo(user.getId());
        redisTemplate.opsForValue()
                .set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(),
                        userLoginVo,
                        RedisConst.USERKEY_TIMEOUT,
                        TimeUnit.DAYS);


        // 数据封装到map中返回
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("token", token);
        map.put("leaderAddressVo", leaderAddressVo);
        return Result.ok(map);
    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}