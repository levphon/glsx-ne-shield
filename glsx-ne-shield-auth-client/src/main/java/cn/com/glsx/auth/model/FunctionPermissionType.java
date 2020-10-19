package cn.com.glsx.auth.model;

/**
 * @author: taoyr
 **/
public enum FunctionPermissionType {

    CHECK_DEVICE(0,"device:check","查看设备");

    FunctionPermissionType(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    /**
     * 权限编号
     */
    private int code;

    /**
     * 权限名
     */
    private String name;

    /**
     * 权限描述
     */
    private String desc;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public FunctionPermissionType getByCode(int code) {
        FunctionPermissionType[] values = FunctionPermissionType.values();
        for (FunctionPermissionType value : values) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
