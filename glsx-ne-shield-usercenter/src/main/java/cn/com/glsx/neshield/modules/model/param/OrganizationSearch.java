package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyr
 */
@Data
@Accessors(chain = true)
public class OrganizationSearch extends Page {

    private boolean forPage;

    private String organizationName;

    private Long enableStatus;

    private Long rootId;

}
