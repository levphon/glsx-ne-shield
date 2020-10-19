package cn.com.glsx.neshield.modules.model;

import lombok.Data;

@Data
public class OrgModel {

    private Long id;
    private Long parentId;
    private Integer depth;
    private Long tenantId;
    private String tenantName;
    private Long deptId;
    private String deptName;
    private Long userNumber;
    private Long orderNum;

}
