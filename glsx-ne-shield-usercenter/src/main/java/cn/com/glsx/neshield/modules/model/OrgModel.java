package cn.com.glsx.neshield.modules.model;

import lombok.Data;

@Data
public class OrgModel {

    private Long orgId;
    private String orgName;
    private Long parentId;
    private Integer depth;

    private Long tenantId;
    private String tenantName;

    private Integer userNumber;
    private Integer orderNum;

}
