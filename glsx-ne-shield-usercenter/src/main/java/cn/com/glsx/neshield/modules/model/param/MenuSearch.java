package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MenuSearch extends Page {

    private String name;
    private Long parentId;
    private Integer enableStatus;

}
