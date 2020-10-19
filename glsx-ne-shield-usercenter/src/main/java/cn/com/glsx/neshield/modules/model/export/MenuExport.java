package cn.com.glsx.neshield.modules.model.export;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class MenuExport {

    @ExcelIgnore
    private Long id;

    @ExcelProperty(value = "菜单名称", index = 0)
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "创建时间", index = 1)
    private Date createdDate;

}
