package cn.shijinshi.fabricmanager.controller;


import cn.shijinshi.fabricmanager.controller.annotation.PassIdentityVerify;
import cn.shijinshi.fabricmanager.controller.entity.Response;
import cn.shijinshi.fabricmanager.controller.entity.user.RegisterUserEntity;
import cn.shijinshi.fabricmanager.controller.entity.user.UpdateUserPasswordEntity;
import cn.shijinshi.fabricmanager.controller.entity.user.UserRemarkEntity;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.User;
import cn.shijinshi.fabricmanager.service.TokenManager;
import cn.shijinshi.fabricmanager.service.UserManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserManageService userManager;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenManager tokenManager;


    @PassIdentityVerify
    @PostMapping()
    public Response register(@RequestBody @Valid RegisterUserEntity register) {
        if (!register.getRootUser() && (register.getRegisterCode() == null || register.getRegisterCode().isEmpty())) {
            return new Response().failure("当注册的用户不是跟用户时需要给定注册码");
        }

        userManager.registerUser(register);

        //login
        User user = new User();
        user.setUserId(register.getUserId());
        user.setPassword(register.getPassword());
        return login(user);
    }

    @DeleteMapping()
    public Response delete(@RequestBody User user, HttpServletRequest request) {
        String delUserId = user.getUserId();

        String affiliation = tokenManager.getRequester(request).getAffiliation();
        User delUser = userService.findUserById(delUserId);
        if (delUser.getAffiliation().startsWith(affiliation)) {
            userManager.deleteUser(delUserId);
        } else {
            return new Response().failure("您没有权限删除用户" + delUserId);
        }

        return new Response().success("用户" + delUserId + "已删除。");
    }

    /**
     * 更新除密码以外的可修改信息
     */
    @PutMapping()
    public Response update(@RequestBody User user) {
        userManager.updateUser(user);
        return new Response().success("用户" + user.getUserId() + "配置修改成功。");
    }

    /**
     * 获取子用户信息
     */
    @GetMapping()
    public Response getChildUsers(HttpServletRequest request) {
        User requester = tokenManager.getRequester(request);
        String userId = requester.getUserId();
        List<User> users = userManager.getChildUser(userId);
        return new Response().success(users);
    }

    /**
     * 获取指定用户信息
     */
    @GetMapping("/{userId}")
    public Response getUsersById(HttpServletRequest request, @PathVariable("userId") String userId) {
        String tokenUserId = tokenManager.getRequester(request).getUserId();
        if (tokenUserId.equals(userId)) {   //只能自己查看自己的信息
            User user = userManager.getUserById(userId);
            return new Response().success(user);
        } else {
            return new Response().failure("No permission to query " + userId + "`s information.");
        }
    }

    /**
     * 获取注册码
     */
    @GetMapping("/registerCode")
    public Response registerCode(HttpServletRequest request) {
        User requester = tokenManager.getRequester(request);
        String code = userManager.getRegisterCode(requester);
        HashMap<String, Object> data = new HashMap<>();
        data.put("code", code);
        return new Response().success(data);
    }

    /**
     * 获取密保问题
     */
    @PassIdentityVerify
    @GetMapping("/secret/{userId}")
    public Response getSecret(@PathVariable("userId") String userId) {
        Map<String, String> questions = userManager.getSecretQuestion(userId);
        return new Response().success(questions);
    }

    /**
     * 更新用户密码
     */
    @PassIdentityVerify
    @PutMapping("/password")
    public Response updatePassword(@RequestBody @Valid UpdateUserPasswordEntity userInfo) {
        userManager.updatePassword(userInfo);
        return new Response().success();
    }

    @PassIdentityVerify
    @PostMapping("/login")
    public Response login(@RequestBody User user) {
        user = userService.checkUser(user.getUserId(), user.getPassword());
        if (user != null) {
            String token = tokenManager.createToken(user);
            userService.updateToken(user.getUserId(), token);
            User newUser = userManager.getUserById(user.getUserId());

            HashMap<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", newUser);
            return new Response().success(data);
        } else {
            return new Response().failure("用户名或密码错误");
        }
    }

    @DeleteMapping("/logout")
    public Response logout(HttpServletRequest request) {
        String userId = tokenManager.getRequester(request).getUserId();
        userService.updateToken(userId, null);
        return new Response().success();
    }

    @PutMapping("/userName")
    public Response serUserName(HttpServletRequest request, @RequestBody User user) {
        String userName = user.getUserName();
        if (userName == null || userName.isEmpty()) {
            return new Response().failure("用户名不可以为空");
        }

        String userId = tokenManager.getRequester(request).getUserId();
        if (userService.updateUserName(userId, userName) == 1) {
            return new Response().success();
        } else {
            return new Response().failure("Update use name Failed.");
        }
    }

    /**
     * 给其他用户设置备注
     */
    @PostMapping("/remark")
    public Response setRemark(@RequestBody @Valid UserRemarkEntity remark, HttpServletRequest request) {
        String userId = tokenManager.getRequester(request).getUserId();
        userManager.setRemark(userId, remark.getUserId(), remark.getRemark());
        return new Response().success();
    }

}
