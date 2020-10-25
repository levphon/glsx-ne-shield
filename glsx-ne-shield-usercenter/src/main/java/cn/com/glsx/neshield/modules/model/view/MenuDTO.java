package cn.com.glsx.neshield.modules.model.view;

import lombok.Data;

@Data
public class MenuDTO {

    private Long id;
    private String menuNo;
    private String menuName;
    private String frontRoute;
    private String permissionTag;
    private Integer type;
    private String icon;
    private Long parentId;
    private Integer orderNum;
    private Integer enableStatus;

}
