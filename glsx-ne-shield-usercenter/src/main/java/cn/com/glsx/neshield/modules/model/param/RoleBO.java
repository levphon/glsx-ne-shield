package cn.com.glsx.neshield.modules.model.param;

import cn.com.glsx.admin.common.util.RegexUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author taoyr
 */
@Data
@Accessors(chain = true)
public class RoleBO implements Serializable {

    private Long roleId;

    @NotBlank
    @Pattern(regexp = RegexUtil.roleNameRegex, message = "名称格式有误")
    @Size(max = 50)
    private String roleName;

    private String remark;

    //数据权限（0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部）
    @NotNull
    private Integer rolePermissionType;

    //角色租户可见度（0=共享，1=系统管理员，2=指定租户）
    @NotNull
    private Integer roleVisibility;

    //可见租户id列表
    private String roleTenants;

    //状态（1正常 ，2停用）
    @NotNull
    private Integer enableStatus;

    //选中菜单列表
    private List<Long> menuIdList;
}
