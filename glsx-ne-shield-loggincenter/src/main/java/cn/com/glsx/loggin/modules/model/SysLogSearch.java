package cn.com.glsx.loggin.modules.model;

import cn.hutool.db.Page;
import lombok.Data;

@Data
public class SysLogSearch extends Page {

    private String sDate;
    private String eDate;

}
