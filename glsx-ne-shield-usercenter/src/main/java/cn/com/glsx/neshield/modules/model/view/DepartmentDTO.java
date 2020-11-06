package cn.com.glsx.neshield.modules.model.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

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
     * 上级id
     */
    private Long superiorId;
    private String superiorName;

    /**
     * 排序值
     */
    private Integer orderNum;

    /**
     * 状态（1=启用 2=禁用）
     */
    private Integer enableStatus;

    private Integer isRoot;

    private boolean hasChildren;

    /**
     * 用户数
     */
    private Integer userNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

}