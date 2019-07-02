package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.controller.entity.user.RegisterUserEntity;
import cn.shijinshi.fabricmanager.controller.entity.user.UpdateUserPasswordEntity;
import cn.shijinshi.fabricmanager.dao.RegisterCodeService;
import cn.shijinshi.fabricmanager.dao.RemarkService;
import cn.shijinshi.fabricmanager.dao.SecretService;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.RegisterCode;
import cn.shijinshi.fabricmanager.dao.entity.Remark;
import cn.shijinshi.fabricmanager.dao.entity.Secret;
import cn.shijinshi.fabricmanager.dao.entity.User;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.utils.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserManageService {

    //注册码三天内有效
    private static final long REGISTER_CODE_EXPIRY_DATE = 68400000 * 3;

    @Autowired
    private StringConverter converter;

    @Autowired
    private UserService userService;
    @Autowired
    private SecretService secretService;
    @Autowired
    private RegisterCodeService registerCodeService;
    @Autowired
    private RemarkService remarkService;

    /**
     * 注册新用户
     *
     * @param register 用户注册信息
     * @throws DuplicateKeyException 主键（用户名）冲突
     */
    @Retryable
    @Transactional
    public void registerUser(RegisterUserEntity register) throws DuplicateKeyException{
        try {
            User user = new User();
            if (register.getRootUser()) {
                user.setIdentity("root");
                user.setAffiliation(register.getUserId());
            } else {
                String code = register.getRegisterCode();
                String affiliation = registerCodeService.findAffiliationByCode(code);
                if (affiliation == null) {
                    throw new ServiceException("无效的注册码");
                }

                registerCodeService.delOverdueCode(REGISTER_CODE_EXPIRY_DATE);
                affiliation = affiliation + "." + register.getUserId();
                user.setIdentity("non-root");
                user.setAffiliation(affiliation);
                registerCodeService.deleteCode(code);
            }
            user.setRegisterDate(System.currentTimeMillis());
            user.setUserId(register.getUserId());
            user.setPassword(register.getPassword());
            userService.register(user);
            setSecret(register);
        } catch (DuplicateKeyException e) {   //
            throw new ServiceException("用户名" + register.getUserId() + "已存在");
        }
    }

    /**
     * 生用户注册码
     * @param user 原始用户信息
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Retryable(value = {ServiceException.class, DuplicateKeyException.class})
    public String getRegisterCode(User user) {
        long time = System.currentTimeMillis();
        String str = user.getUserId() + time + Math.random();
        String code = converter.getMD5(str);
        RegisterCode registerCode = new RegisterCode();
        registerCode.setCode(code);
        registerCode.setOwner(user.getUserId());
        registerCode.setGenerateDate(time);
        if (registerCodeService.addCode(registerCode) == 1) {
            return code;
        } else {
            throw new ServiceException("生成注册码失败");
        }
    }

    /**
     * 删除用户
     * 数据库外键约束会级联删除secret,register_code,remark对应数据
     * @param userId 用户id
     */
    @Transactional
    public void deleteUser(String userId) {
        userService.deleteUser(userId);
    }

    /**
     * 更新除密码以外的用户可修改信息
     * @param user
     */
    public void updateUser(User user) {
        //重新创建一个新的user对象，避免不小心更新了不能更改的数据
        User newUser = new User();
        newUser.setUserId(user.getUserId());
        newUser.setUserName(user.getUserName());
        userService.updateUserSelective(newUser);
    }

    /**
     * 更新用户密码
     * @param userInfo
     */
    public void updatePassword(UpdateUserPasswordEntity userInfo) {
        Map<String, String> secretMap = getSecret(userInfo.getUserId());
        String ans1 = secretMap.get(userInfo.getQuestion1());
        if (ans1 == null || ans1.isEmpty() || !ans1.equals(converter.getMD5(userInfo.getAnswer1()))) {
            throw new ServiceException("密保验证失败");
        }
        String ans2 = secretMap.get(userInfo.getQuestion2());
        if (ans2 == null || ans2.isEmpty() || !ans2.equals(converter.getMD5(userInfo.getAnswer2()))) {
            throw new ServiceException("密保验证失败");
        }
        String ans3 = secretMap.get(userInfo.getQuestion3());
        if (ans3 == null || ans3.isEmpty() || !ans3.equals(converter.getMD5(userInfo.getAnswer3()))) {
            throw new ServiceException("密保验证失败");
        }

        User user = new User();
        user.setUserId(userInfo.getUserId());
        user.setPassword(userInfo.getPassword());
        userService.updateUserSelective(user);
    }

    /**
     * 获取子用户
     * @param userId 用户id
     * @return
     */
    public List<User> getChildUser(String userId) {
        List<User> users = userService.getChildUser(userId);
        Map<String, String> remarksMap = remarkService.getRemarksByParent(userId);

        //重新创建新的userList避免不小心暴露隐私信息
        List<User> userList = new ArrayList<>();
        for (User user : users) {
            User newUser = new User();
            newUser.setUserId(user.getUserId());
            newUser.setUserName(remarksMap.get(user.getUserId()));
            newUser.setAffiliation(user.getAffiliation());
            newUser.setRegisterDate(user.getRegisterDate());

            userList.add(newUser);
        }
        return userList;
    }

    /**
     * 获取指定用户信息
     * @param userId 用户id
     * @return
     */
    public User getUserById(String userId) {
        User user = userService.findUserById(userId);

        User newUser = new User();
        newUser.setUserId(user.getUserId());
        newUser.setUserName(user.getUserName());
        newUser.setIdentity(user.getIdentity());
        newUser.setAffiliation(user.getAffiliation());
        newUser.setRegisterDate(user.getRegisterDate());
        return newUser;
    }

    /**
     * 设置密保信息
     * @param register 注册信息
     */
    private void setSecret(RegisterUserEntity register) {
        Secret secret = new Secret();
        secret.setUserId(register.getUserId());
        secret.setQuestion1(register.getQuestion1());
        secret.setAnswer1(converter.getMD5(register.getAnswer1()));
        secret.setQuestion2(register.getQuestion2());
        secret.setAnswer2(converter.getMD5(register.getAnswer2()));
        secret.setQuestion3(register.getQuestion3());
        secret.setAnswer3(converter.getMD5(register.getAnswer3()));
        secretService.addSecret(secret);
    }

    public Map<String, String> getSecretQuestion(String userId) {
        Secret secret = secretService.getSecret(userId);
        if (secret == null) throw new ServiceException("不存在用户:" + userId);

        HashMap<String, String> secretQuestion = new HashMap<>();
        secretQuestion.put("question1", secret.getQuestion1());
        secretQuestion.put("question2", secret.getQuestion2());
        secretQuestion.put("question3", secret.getQuestion3());
        return secretQuestion;
    }

    public Map<String,String> getSecret(String userId) {
        Secret secret = secretService.getSecret(userId);
        if (secret == null) throw new ServiceException("不存在用户:" + userId);

        HashMap<String, String> secretMap = new HashMap<>();
        secretMap.put(secret.getQuestion1(), secret.getAnswer1());
        secretMap.put(secret.getQuestion2(), secret.getAnswer2());
        secretMap.put(secret.getQuestion3(), secret.getAnswer3());
        return secretMap;
    }

    public int setRemark(String parentUserId, String userId, String remarks) {
        Remark remark = new Remark();
        remark.setParentUserId(parentUserId);
        remark.setUserId(userId);
        remark.setRemarks(remarks);
        try {
            return remarkService.insertRemark(remark);
        } catch (DuplicateKeyException e) {
            return remarkService.updateRemark(remark);
        }
    }

}
