package cn.com.glsx.neshield.modules.model.view;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class OrganizationListView implements Serializable {

    private boolean hasChild;

    private String organizationName;

    private Integer orderNum;

    private Integer enableStatus;

    private String createDate;

}
