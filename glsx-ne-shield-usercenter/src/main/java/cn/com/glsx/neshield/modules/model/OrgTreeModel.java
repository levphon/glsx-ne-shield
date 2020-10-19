package cn.com.glsx.neshield.modules.model;

import com.glsx.plat.common.model.TreeModel;

import java.util.ArrayList;
import java.util.List;

public class OrgTreeModel implements TreeModel<OrgModel> {

    protected OrgModel org;

    private Long userNumber;

    private Long orderNum;

    private List<TreeModel> children = new ArrayList<>();

    public OrgTreeModel(OrgModel org) {
        this.org = org;
        this.userNumber = org.getUserNumber();
        this.orderNum = org.getOrderNum();
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    @Override
    public Long getId() {
        return org.getId();
    }

    @Override
    public Long getParentId() {
        return org.getParentId();
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(Long userNumber) {
        this.userNumber = userNumber;
    }

    @Override
    public String getLabel() {
        return org.getDeptName();
    }

    @Override
    public Integer getDepth() {
        return org.getDepth();
    }

    @Override
    public Integer getOrder() {
        return 0;
    }

    @Override
    public boolean checked() {
        return false;
    }

    @Override
    public OrgModel getOrigin() {
        return org;
    }

    @Override
    public List<TreeModel> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List data) {
        this.children = data;
    }

}
