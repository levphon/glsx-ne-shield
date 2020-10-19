package cn.com.glsx.base.modules.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DictDataDTO implements Serializable {

    private Long id;
    private String dictLabel;
    private String dictValue;
    private String dictType;
    private Integer dictSort;
    private String isDefault;
    private Integer enableStatus;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

}
