package cn.com.glsx.neshield.modules.entity;

import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Accessors(chain = true)
@Data
@Table(name = "t_user")
public class User extends BaseEntity {


    public User() {
        super();
    }

    public User(boolean addOrUpdate) {
        SyntheticUser user = ShieldContextHolder.getUser();
        if (addOrUpdate) {
            this.setCreatedBy(user.getUserId());
            this.setUpdatedBy(user.getUserId());
            this.setCreatedDate(new Date());
            this.setUpdatedDate(new Date());
        } else {
            this.setUpdatedBy(user.getUserId());
            this.setUpdatedDate(new Date());
        }
    }

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 部门id
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 上级id
     */
    @Column(name = "superior_id")
    private Long superiorId;

    /**
     * 账户名
     */
    @Column(name = "account")
    private String account;

    /**
     * 头像
     */
    @Column(name = "portrait")
    private String portrait;

    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 性别
     */
    @Column(name = "gender")
    private Long gender;

    /**
     * 岗位
     */
    @Column(name = "position")
    private Long position;

    @Column(name = "enable_status")
    private Integer enableStatus;

    private String salt;

}