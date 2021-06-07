package models;

import pers.xuankai.datamysql.InsertRequired;
import pers.xuankai.datamysql.PrimaryKey;

import java.time.LocalDateTime;

public class User {

    public User() {
    }

    public User(String name, LocalDateTime birth, String sex) {
        Name = name;
        Birth = birth;
        Sex = sex;
    }

    @PrimaryKey
    private int Id;
    @InsertRequired
    private String Name;
    @InsertRequired
    private LocalDateTime Birth;
    @InsertRequired
    private String Sex;

    public int getId() {
        return Id;
    }

    public User setId(int id) {
        Id = id;
        return this;
    }

    public String getName() {
        return Name;
    }

    public User setName(String name) {
        Name = name;
        return this;
    }

    public LocalDateTime getBirth() {
        return Birth;
    }

    public User setBirth(LocalDateTime birth) {
        Birth = birth;
        return this;
    }

    public String getSex() {
        return Sex;
    }

    public User setSex(String sex) {
        Sex = sex;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", Birth=" + Birth +
                ", Sex='" + Sex + '\'' +
                '}';
    }
}
