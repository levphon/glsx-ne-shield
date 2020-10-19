package cn.com.glsx.neshield.modules.model.view;

import cn.com.glsx.neshield.modules.entity.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class DepartmentDTO {

    private Long id;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 排序值
     */
    private Long orderNum;

    /**
     * 状态（1=启用 2=禁用）
     */
    private Long enableStatus;

    private Integer isRoot;

    private boolean hasChildren;

    private Long userNumber;

    private List<DepartmentDTO> departmentDtoList;

    private List<User> userList;

}