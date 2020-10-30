package cn.com.glsx.auth.model;

import java.lang.annotation.*;

/**
 * 需要数据权限
 *
 * @author: taoyr
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireDataPermissions {

    /**
     * @return
     */
    String linkTable() default "";

    /**
     * 需要进行过滤的连表id(增删改操作不需要)
     *
     * @return
     */
    String linkField() default "";

}
