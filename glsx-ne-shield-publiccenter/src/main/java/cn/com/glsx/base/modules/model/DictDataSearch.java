package cn.com.glsx.base.modules.model;

import cn.hutool.db.Page;
import lombok.Data;

@Data
public class DictDataSearch extends Page {

    private String name;
    private String type;
    private String tag;
    private Integer status;
    private String sDate;
    private String eDate;

}
