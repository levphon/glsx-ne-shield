package cn.com.glsx.neshield.modules.model.view;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyr
 */
@Data
@Accessors(chain = true)
public class DepartmentUserCount {

    private Long departmentId;

    private Long userNumber;

}
