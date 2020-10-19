package cn.com.glsx.auth.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Department {

    private Long deptId;
    private String departmentName;
    private Long tenantId;

}
