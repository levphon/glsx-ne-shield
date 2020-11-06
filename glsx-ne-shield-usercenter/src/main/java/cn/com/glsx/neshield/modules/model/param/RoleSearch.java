package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class RoleSearch extends Page {

    private String roleName;

    private Integer enableStatus;

}
