package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;

@Data
public class MenuSearch extends Page {

    private String name;
    private Integer status;
    private Long parentId;

}
