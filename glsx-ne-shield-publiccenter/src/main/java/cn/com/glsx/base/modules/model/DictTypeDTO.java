package cn.com.glsx.base.modules.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DictTypeDTO implements Serializable {

    private Long id;
    private String dictName;
    private String dictType;
    private String remark;
    private Integer enableStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
    @JsonIgnore
    private Integer delFlag;
}
