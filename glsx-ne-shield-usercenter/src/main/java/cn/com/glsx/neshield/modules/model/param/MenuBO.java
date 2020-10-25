package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class MenuBO implements Serializable {

    private Long id;
    private Integer type;
    private Long parentId;
    private String menuName;
    private String permissionTag;
    private String frontRoute;
    private Integer orderNum;
    private Integer enableStatus;
    private String icon;

}
