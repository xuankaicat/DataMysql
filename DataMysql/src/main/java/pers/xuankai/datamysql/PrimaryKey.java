package pers.xuankai.datamysql;

import java.lang.annotation.*;

/**
 * 主键，作为更新时的凭据。
 * 在使用DataMysql.INSTANCE.update方法时传入的对象类中所有带该注解的属性在作为被更新属性的同时还作为本次更新的查询属性。
 * @author pers.xuankai
 * @version 1.0
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface PrimaryKey {

}