package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class RegisterCode implements Serializable {
    /**
     * 。
    * <p> column ==>code</p>
     */
    private String code;

    /**
     * 。
    * <p> column ==>owner</p>
     */
    private String owner;

    /**
     * 。
    * <p> column ==>generate_date</p>
     */
    private Long generateDate;

    /**
     * register_code。
    * <p> table ==>RegisterCode</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #code}
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置 。
     * @param code 
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取 。
     * @return {@link #owner}
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 设置 。
     * @param owner 
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 获取 。
     * @return {@link #generateDate}
     */
    public Long getGenerateDate() {
        return generateDate;
    }

    /**
     * 设置 。
     * @param generateDate 
     */
    public void setGenerateDate(Long generateDate) {
        this.generateDate = generateDate;
    }
}