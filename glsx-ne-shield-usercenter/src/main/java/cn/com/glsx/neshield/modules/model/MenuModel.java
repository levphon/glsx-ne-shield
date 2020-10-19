package cn.com.glsx.neshield.modules.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Data
public class MenuModel implements Serializable {

    private Long menuNo;
    private String menuName;
    private Long parentId;
    private Integer orderNum;
    private Integer type;
    private boolean checked;

}
