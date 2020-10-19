package cn.com.glsx.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class Permission {

    private Integer permissionId;

    private Integer menuId;

    private String interfaceUrl;


}
