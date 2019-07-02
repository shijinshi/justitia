package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class Secret implements Serializable {
    /**
     * 。
    * <p> column ==>user_id</p>
     */
    private String userId;

    /**
     * 。
    * <p> column ==>question1</p>
     */
    private String question1;

    /**
     * 。
    * <p> column ==>answer1</p>
     */
    private String answer1;

    /**
     * 。
    * <p> column ==>question2</p>
     */
    private String question2;

    /**
     * 。
    * <p> column ==>answer2</p>
     */
    private String answer2;

    /**
     * 。
    * <p> column ==>question3</p>
     */
    private String question3;

    /**
     * 。
    * <p> column ==>answer3</p>
     */
    private String answer3;

    /**
     * secret。
    * <p> table ==>Secret</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #userId}
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置 。
     * @param userId 
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 。
     * @return {@link #question1}
     */
    public String getQuestion1() {
        return question1;
    }

    /**
     * 设置 。
     * @param question1 
     */
    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    /**
     * 获取 。
     * @return {@link #answer1}
     */
    public String getAnswer1() {
        return answer1;
    }

    /**
     * 设置 。
     * @param answer1 
     */
    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    /**
     * 获取 。
     * @return {@link #question2}
     */
    public String getQuestion2() {
        return question2;
    }

    /**
     * 设置 。
     * @param question2 
     */
    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    /**
     * 获取 。
     * @return {@link #answer2}
     */
    public String getAnswer2() {
        return answer2;
    }

    /**
     * 设置 。
     * @param answer2 
     */
    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    /**
     * 获取 。
     * @return {@link #question3}
     */
    public String getQuestion3() {
        return question3;
    }

    /**
     * 设置 。
     * @param question3 
     */
    public void setQuestion3(String question3) {
        this.question3 = question3;
    }

    /**
     * 获取 。
     * @return {@link #answer3}
     */
    public String getAnswer3() {
        return answer3;
    }

    /**
     * 设置 。
     * @param answer3 
     */
    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }
}