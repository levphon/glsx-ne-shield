package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class OrganizationListParam extends Page implements Serializable {

    private Long organizationId;

}
