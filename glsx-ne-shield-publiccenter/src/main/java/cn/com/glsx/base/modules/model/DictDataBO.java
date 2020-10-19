package cn.com.glsx.base.modules.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DictDataBO implements Serializable {

    private Long id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String isDefault;
    private Integer enableStatus;
    private String remark;

}
