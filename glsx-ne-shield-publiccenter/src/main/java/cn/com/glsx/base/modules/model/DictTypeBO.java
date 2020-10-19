package cn.com.glsx.base.modules.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class DictTypeBO implements Serializable {

    private Long id;
    private String dictName;
    private String dictType;
    private String remark;
    private Integer enableStatus;

}
