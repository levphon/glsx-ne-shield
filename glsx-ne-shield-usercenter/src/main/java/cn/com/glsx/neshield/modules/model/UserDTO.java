package cn.com.glsx.neshield.modules.model;

import lombok.Data;

import java.util.Date;

/**
 * @author payu
 */
@Data
public class UserDTO {

    private Long id;

    private String account;

    private String username;

    private String departmentName;

    private String phoneNumber;

    /**
     * 部门id
     */
    private Long departmentId;

    /**
     * 上级id
     */
    private Long superiorId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private Long gender;

    /**
     * 岗位
     */
    private Long position;

    private Integer enableStatus;

    private Long roleId;

    private String roleName;

    private String superiorName;

    private Date createdDate;

}
