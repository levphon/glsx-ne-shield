package cn.com.glsx.neshield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class UserSearch extends Page {

    private String searchField;

    private Integer userStatus;

    private Long departmentId;

    private List<Long> departmentIdList;

    private Long userId;

}
