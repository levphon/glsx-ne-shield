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

    private Long superiorId;

    private Long organizationId;

    private String name;

    private Integer orderNum;

    private Integer enableStatus;

    private List<Long> subIdList;

    private List<Long> superiorIdList;

    private Integer biggerDepth;

}
