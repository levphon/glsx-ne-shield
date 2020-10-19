package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Accessors(chain = true)
@Data
public class UserParam implements Serializable {

    private Long id;

    /**
     * 用户名
     */
    @NotEmpty
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
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
     * 账户名
     */
    private String account;

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

}
