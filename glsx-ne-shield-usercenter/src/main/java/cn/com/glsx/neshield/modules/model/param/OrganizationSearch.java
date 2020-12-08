package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author taoyr
 */
@Data
@Accessors(chain = true)
public class OrganizationSearch extends Page {

    private boolean forPage;

    private String orgName;

    private Integer enableStatus;

    private Long rootId;

    private boolean hasChild;

    private boolean hasUserNumber;

    private Long tenantId;

    private Collection<Long> orgIds;

}
