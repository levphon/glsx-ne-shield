package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author payu
 */
@Data
public class OrgTreeSearch {

    private Long orgId;

    private String orgName;

    private List<Long> orgIds;

}
