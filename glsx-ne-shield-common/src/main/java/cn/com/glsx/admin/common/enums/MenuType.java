package cn.com.glsx.admin.common.enums;

import lombok.Getter;

/**
 * @author payu
 */

@Getter
public enum MenuType {

    DIRECTORY(1, "目录"), MENU(2, "菜单"), BUTTON(2, "按钮");

    private Integer code;
    private String type;

    MenuType(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

}
