package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.User;
import cn.shijinshi.fabricmanager.dao.mapper.UserMapper;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.utils.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper mapper;

    public int register(User user) throws DuplicateKeyException {
        StringConverter converter = new StringConverter();
        String passwordMD5 = converter.getMD5(user.getPassword());
        user.setPassword(passwordMD5);
        return mapper.insertSelective(user);
    }

    public User checkUser(String userId, String password) {
        StringConverter converter = new StringConverter();
        String passwordMD5 = converter.getMD5(password);

        User user = mapper.selectByPrimaryKey(userId);
        if (user != null && passwordMD5.equals(user.getPassword())) {
            return user;
        } else {
            return null;
        }
    }

    public int selectUserCount() {
        return mapper.selectUserCount();
    }

    public User findUserById(String userId) {
        return mapper.selectByPrimaryKey(userId);
    }

    public int deleteUser(String userId) {
        return mapper.deleteByPrimaryKey(userId);
    }

    private List<User> getUsersByAffiliation(String affiliation) {
        return mapper.getUsersByAffiliation(affiliation + "%");
    }

    public List<User> getChildUser(String userId) {
        User user = mapper.selectByPrimaryKey(userId);
        return getUsersByAffiliation(user.getAffiliation() + ".");
    }

    public boolean isSelfOrChild(String userId, String childId) {
        if (userId == null || childId == null) {
            throw new ServiceException("UserId or childId is null.");
        } else if (userId.equals(childId)) {
            User user = findUserById(userId);
            if (user == null) {
                throw new ServiceException("Not found user by Id.");
            } else {
                return true;
            }
        }

        User childUser = findUserById(childId);
        User parentUser = findUserById(userId);
        if (parentUser == null || childUser == null) {
            throw new ServiceException("Not found user by id.");
        }

        return childUser.getAffiliation().startsWith(parentUser.getAffiliation() + ".");
    }

    public int updateToken(String userId, String token) {
        return mapper.updateToken(userId, token);
    }

    public int updateUserSelective(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            StringConverter converter = new StringConverter();
            user.setPassword(converter.getMD5(user.getPassword()));
        }
        return mapper.updateByPrimaryKeySelective(user);
    }

    public int updateUserName(String userId, String userName) {
        return mapper.updateUserName(userId, userName);
    }


}
