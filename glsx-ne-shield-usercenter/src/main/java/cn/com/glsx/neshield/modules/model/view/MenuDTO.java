package cn.com.glsx.neshield.modules.model.view;

import lombok.Data;

@Data
public class MenuDTO {

    private Long id;
    private Long menuNo;
    private String menuName;
    private String frontRoute;
    private String permissionTag;
    private Integer type;
    private String icon;
    /**
     * 父级物理主键id
     */
    private Long ppId;
    /**
     * 父级菜单编号menuNo
     */
    private Long parentId;
    private String parentName;
    private boolean hasChildren;
    private Integer orderNum;
    private Integer enableStatus;

}
