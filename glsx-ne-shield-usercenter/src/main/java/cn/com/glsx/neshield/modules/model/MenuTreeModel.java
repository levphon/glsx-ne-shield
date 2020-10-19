package cn.com.glsx.neshield.modules.model;


import com.glsx.plat.common.model.TreeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author: taoyr
 **/
public class MenuTreeModel implements TreeModel<MenuModel> {

    protected MenuModel menu;

    protected List<TreeModel> children = new ArrayList<>();

    public MenuTreeModel(MenuModel menu) {
        this.menu = menu;
    }

    @Override
    public Long getId() {
        return menu.getMenuNo();
    }

    @Override
    public Long getParentId() {
        return menu.getParentId();
    }

    @Override
    public String getLabel() {
        return menu.getMenuName();
    }

    @Override
    public Integer getDepth() {
        return null;
    }

    @Override
    public Integer getOrder() {
        return menu.getOrderNum();
    }

    @Override
    public boolean checked() {
        return menu.isChecked();
    }

    @Override
    public MenuModel getOrigin() {
        return menu;
    }

    @Override
    public List getChildren() {
        return children;
    }

    @Override
    public void setChildren(List data) {
        List<MenuModel> menuModelList = (List<MenuModel>) data;
        Collections.sort(menuModelList, Comparator.comparing(MenuModel::getOrderNum));
        this.children = data;
    }

}
