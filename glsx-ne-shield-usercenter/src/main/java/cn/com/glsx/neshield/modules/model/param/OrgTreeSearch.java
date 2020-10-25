package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class OrgTreeSearch {

    private Long tenantId;

    private Long orgId;

    private String orgName;

    private List<Long> orgIds;

}
