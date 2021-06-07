package pers.xuankai.datamysql;

import java.lang.annotation.*;

/**
 * 插入时必须存在的字段。
 * 在使用DataMysql.INSTANCE.insert方法时传入的对象类中所有带该注解的属性是插入时必须的，不应该为空。
 * @author pers.xuankai
 * @version 1.0
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface InsertRequired {

}