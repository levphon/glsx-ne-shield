package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class OrgTreeSearch {

    private Long tenantId;

    private Long orgId;

    private String orgName;

    private Integer enableStatus;

    private Collection<Long> orgIds;

    private Integer rolePermissionType;

}
