package cn.com.glsx.neshield.modules.model.param;

import cn.com.glsx.admin.common.util.RegexUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class UserBO {

    private Long id;

    @Size(max = 50)
    @NotBlank
    private String account;

    /**
     * 部门id
     */
    @NotNull
    private Long departmentId;

    /**
     * 上级id
     */
    private Long superiorId;

    @Size(max = 50)
    @NotBlank
    private String username;

    private String password;

    @NotNull
    private Long roleId;

    @NotBlank
    @Pattern(regexp = RegexUtil.mobileRegex)
    private String phoneNumber;

    private String email;

    private Long gender;

    /**
     * 岗位
     */
    private Long position;

    @NotNull
    private Integer enableStatus;

    private String remark;

    private Long tenantId;

    private List<Long> departmentIds;

}
