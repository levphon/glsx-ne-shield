package cn.com.glsx.admin.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taoyr
 */
public class RegexUtil {

    //public static final String pwdRegex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,18}$";
    public static final String pwdRegex = "^[0-9A-Za-z]{6,18}$";

    public static final String mobileRegex = "^1(3|4|5|7|8|9)\\d{9}$";

    public static final String roleNameRegex = "^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";

    public static boolean regexPwd(String pwd) {
        return pwd.matches(pwdRegex);
    }

    public static void main(String[] args) {
        String str = "新增测试角色2_444";

        Pattern p = Pattern.compile(roleNameRegex);
        Matcher m = p.matcher(str);
        boolean flag = m.matches();
        System.out.println(flag);
    }

}
