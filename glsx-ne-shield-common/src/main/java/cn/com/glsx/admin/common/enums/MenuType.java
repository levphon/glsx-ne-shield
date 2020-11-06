package cn.com.glsx.admin.common.enums;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * @author payu
 */

@Getter
public enum MenuType {

    DIRECTORY(1, "目录"), MENU(2, "菜单"), BUTTON(3, "按钮");

    private Integer code;
    private String type;

    MenuType(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public static List<Integer> getAllTypes() {
        List<Integer> list = Lists.newArrayList();
        MenuType[] types = MenuType.values();
        for (MenuType type : types) {
            list.add(type.getCode());
        }
        return list;
    }

}
