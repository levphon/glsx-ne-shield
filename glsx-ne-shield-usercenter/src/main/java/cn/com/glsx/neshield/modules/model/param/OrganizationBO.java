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

    private Long id;

    private String departmentName;

    private Long superiorId;

    private Integer orderNum;

    private Integer enableStatus;

    private List<Long> subIdList;

    private List<Long> superiorIdList;

    private Integer biggerDepth;

}
