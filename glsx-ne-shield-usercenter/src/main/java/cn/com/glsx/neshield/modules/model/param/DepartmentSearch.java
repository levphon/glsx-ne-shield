package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

@Data
@Accessors(chain = true)
public class DepartmentSearch {

    private Long tenantId;
    private String departmentName;
    private Integer enableStatus;
    private Integer isRoot;
    private Collection<Long> orgIds;

}
