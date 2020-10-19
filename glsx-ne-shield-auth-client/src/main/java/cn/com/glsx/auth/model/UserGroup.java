package cn.com.glsx.auth.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserGroup {

    private Long groupId;
    private String userGroupName;
    private Long tenantId;

}
