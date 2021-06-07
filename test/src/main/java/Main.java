import pers.xuankai.datamysql.DataMysql;
import models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        DataMysql dataMysql = DataMysql.INSTANCE;
        dataMysql.setDatabaseName("jdbctest")
                .setUser("root", "280814");
        ArrayList<User> userList;

        userList = dataMysql.query(User.class);//查询一张表中所有的内容
        //userList = dataMysql.query(User.class, "select * from user"); //根据sql语句查询
        userList.forEach(user -> System.out.println(user.toString()));

        System.out.println("查询ID为1的用户：");
        User userObj = new User().setId(1);
        userList = dataMysql.query(userObj);//根据实例查询
        //userList = dataMysql.query(User.class, "select * from user where Id=1"); //根据sql语句查询
        assert userList != null;
        userList.forEach(user -> System.out.println(user.toString()));

        System.out.println("查询Name为wzp并且性别为未知的用户：");
        userObj = new User().setName("wzp").setSex("未知");
        userList = dataMysql.query(userObj, "Name", "Sex");//根据实例查询
        //userList = dataMysql.query(User.class, "select * from user where Name='wzp' and Sex='未知'"); //根据sql语句查询
        assert userList != null;
        userList.forEach(user -> System.out.println(user.toString()));

        System.out.println("插入用户gcm：");
        User gcm = new User("gcm", LocalDateTime.now(), "男");
        Integer newId = dataMysql.insert(gcm); //根据实例插入
        //Integer newId = dataMysql.executeInsert("insert into user(name, birth, sex) " +
        //        "values('gcm', " + LocalDateTime.now() + ", '男')"); //根据sql语句插入

        System.out.println("gcm的ID是" + newId);
        gcm.setId(newId);

        userList = dataMysql.query(User.class, "select * from user");
        assert userList != null;
        userList.forEach(user -> System.out.println(user.toString()));

        System.out.println("设置gcm性别为未知：");
        gcm.setSex("未知");
        dataMysql.update(gcm); //根据实例更新
        //dataMysql.update("update user set sex='未知' where id=" + gcm.getId()); //根据sql语句更新

        userList = dataMysql.query(User.class, "select * from user");
        assert userList != null;
        userList.forEach(user -> System.out.println(user.toString()));

        System.out.println("删除用户gcm...");
        dataMysql.delete(gcm); //根据实例删除
        //dataMysql.executeSql("delete from user where id=" + gcm.getId()); //根据sql语句删除
    }
}
