package cn.com.glsx.neshield.modules.service.permissionStrategy;

import cn.com.glsx.neshield.modules.model.view.DepartmentDTO;
import com.glsx.plat.common.model.TreeModel;

import java.util.List;

/**
 * @author taoyr
 */
public interface PermissionStrategy {

    List<DepartmentDTO> organizationSimpleList(Long rootId);

    List<? extends TreeModel> orgTree(String departmentName);

}
