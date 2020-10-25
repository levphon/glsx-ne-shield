package cn.com.glsx.neshield.modules.model.view;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author taoyr
 */
@Accessors(chain = true)
@Data
public class SuperTreeModel implements Serializable {

    private Long id;
    private String label;
    private Integer order = 0;
    private boolean isRoot;
    private List<SuperTreeModel> children;

}
