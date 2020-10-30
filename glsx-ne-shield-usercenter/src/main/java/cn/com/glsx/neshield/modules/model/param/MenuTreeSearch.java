package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class MenuTreeSearch {

    private List<Long> roleIds;

    private List<Long> menuIds;

    private List<Integer> menuTypes;

}
