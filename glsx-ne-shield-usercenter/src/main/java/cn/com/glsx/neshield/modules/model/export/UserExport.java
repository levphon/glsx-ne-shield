package cn.com.glsx.neshield.modules.model.export;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UserExport {

    @ExcelIgnore
    private Long id;

    @ExcelProperty(value = "用户名", index = 0)
    private String username;

    @ExcelProperty(value = "手机号码", index = 1)
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "创建时间", index = 2)
    private Date createdDate;

}
