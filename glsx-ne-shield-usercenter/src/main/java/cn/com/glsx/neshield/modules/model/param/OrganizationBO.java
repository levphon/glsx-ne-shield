package cn.com.glsx.neshield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class OrganizationBO {

    private Long rootId;

    private Long organizationId;

    private String name;

    private Long orderNum;

    private Long enableStatus;

    private List<Long> subIdList;

    private List<Long> superiorIdList;

    private Integer biggerDepth;

}
