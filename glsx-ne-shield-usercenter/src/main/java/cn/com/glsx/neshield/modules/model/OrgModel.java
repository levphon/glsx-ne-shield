package cn.com.glsx.neshield.modules.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrgModel {

    private Long orgId;
    private String orgName;
    private Long parentId;
    private Long tenantId;

    @JsonIgnore
    private Integer depth;
    @JsonIgnore
    private Integer userNumber;
    @JsonIgnore
    private Integer orderNum;

    public Integer getUserNumber() {
        return userNumber == null ? 0 : userNumber;
    }

}
