package pers.xuankai.datamysql;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 数据库操作类
 * @author xuankai
 * @version 1.0
 **/
public final class DataMysql {
    private String urlRoot = "127.0.0.1:3306";
    private String databaseName = "default";
    private String username = "root";
    private String password = "123456";

    public static void main(String[] args) {

    }

    /**
     * 设置数据库的url访问路径，默认为127.0.0.1:3306
     * @author xuankai
     * @param urlRoot url路径
     * @return DataMysql实例
     **/
    public DataMysql setUrlRoot(String urlRoot) {
        this.urlRoot = urlRoot;
        return this;
    }

    /**
     * 设置数据库名称，默认为default
     * @author xuankai
     * @param databaseName 数据库名称
     * @return DataMysql实例
     **/
    public DataMysql setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    /**
     * 设置访问数据库的用户名称与密码，默认为root，123456
     * @author xuankai
     * @param username 用户名称
     * @param password 密码
     * @return DataMysql实例
     **/
    public DataMysql setUser(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    private DataMysql(){}

    /**
     * 该类的唯一实例。
     * 在其他地方使用此类应该使用DataMysql sql = DataMysql.INSTANCE而不是new DataMysql()
     **/
    public static final DataMysql INSTANCE;
    static {
        INSTANCE = new DataMysql();
    }

    private Connection openConnection() {
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://"+ urlRoot +"/"+databaseName+"?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&tinyInt1isBit=false",
                    username, password);
        } catch (Exception e) {
            System.out.println("Mysql连接失败");
            connection = null;
        }
        return connection;
    }

    private void closeConnection(Connection conn, Statement stmt) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.out.println("Mysql连接失败");
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                System.out.println("Mysql连接失败");
            }
        }
    }

    /**
     * 在sql中插入对象obj。
     * 使用插入之后原对象中不需要插入的属性(如自增的ID)并不会自动设置，需要手动设置。
     * @author xuankai
     * @param <T> 对象类型
     * @param obj T 要插入的对象
     * @return Integer 插入的自增ID，运行失败或自增ID不存在则返回null
     **/
    public final <T> Integer insert(T obj) {
        StringBuilder sql = new StringBuilder("insert into " + obj.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "(");
        boolean firstAdd = true;
        StringBuilder value = new StringBuilder("values(");

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(InsertRequired.class)) {
                if (firstAdd) {
                    firstAdd = false;
                } else {
                    sql.append(',');
                    value.append(',');
                }
                sql.append(f.getName());
                try {
                    f.setAccessible(true);
                    if(f.getType().isAssignableFrom(Number.class)){
                        value.append(f.get(obj));
                    } else {
                        value.append('\'').append(f.get(obj)).append('\'');
                    }
                } catch (Exception e) {
                    System.out.println(obj + "不是合适的数据库对象");
                }
            }
        }
        sql.append(") ");
        sql.append(value).append(')');
        return executeInsert(sql.toString());
    }

    /**
     * 运行sql插入语句。
     * @author xuankai
     * @param sql String sql语句
     * @return Integer 新插入的自增ID，运行失败或自增ID不存在则返回null
     **/
    public final Integer executeInsert(String sql) {
        Connection conn = openConnection();
        Statement stmt = null;
        Integer result = null;
        try {
            stmt = conn != null ? conn.createStatement() : null;
            if(stmt == null) {
                throw new Exception();
            }
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if(rs == null) {
                return null;
            }
            if(rs.next()) {
                result = rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Mysql操作失败,语句为" + sql);
            closeConnection(conn, stmt);
            return null;
        }
        closeConnection(conn, stmt);
        return result;
    }

    /**
     * 更新在sql中的obj对象
     * @author xuankai
     * @param <T> 对象类型
     * @param obj T 数据库中已经存在的对象
     * @return 返回语句是否正确,传入对象不正确则返回null
     **/
    public final <T> Boolean update(T obj) {
        StringBuilder sql = new StringBuilder("update " + obj.getClass().getSimpleName().toLowerCase(Locale.ROOT) + " set ");
        boolean firstAdd = true;
        boolean firstPK = true;
        StringBuilder whereStr = new StringBuilder(" where ");

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                if (firstPK) {
                    firstPK = false;
                } else {
                    whereStr.append(" and ");
                }
                if (appendObjField(obj, whereStr, f)) return null;
            }
            if (firstAdd) {
                firstAdd = false;
            } else {
                sql.append(',');
            }
            if (appendObjField(obj, sql, f)) return null;
        }
        if(!firstPK) sql.append(whereStr);

        return executeSql(sql.toString());
    }

    /**
     * 删除在sql中的obj对象。
     * 注意，这个方法只能传入带主键的sql类型，否则会出现奇怪的问题
     * @author xuankai
     * @param <T> 对象类型
     * @param obj T 数据库中已经存在的对象
     * @return 返回语句是否正确,传入对象不正确则返回null
     **/
    public final <T> Boolean delete(T obj) {
        StringBuilder sql = new StringBuilder("delete from " + obj.getClass().getSimpleName().toLowerCase(Locale.ROOT) + " where ");
        boolean firstPK = true;

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                if (firstPK) {
                    firstPK = false;
                } else {
                    sql.append(" and ");
                }
                if (appendObjField(obj, sql, f)) return null;
            }
        }

        return executeSql(sql.toString());
    }

    private static <T> boolean appendObjField(T obj, StringBuilder builder, Field field) {
        builder.append(field.getName()).append("=");
        try {
            field.setAccessible(true);
            if(field.getType().isAssignableFrom(Number.class)){
                builder.append(field.get(obj));
            } else {
                builder.append('\'').append(field.get(obj)).append('\'');
            }
        } catch (Exception e) {
            System.out.println(obj + "不是合适的数据库对象");
            return true;
        }
        return false;
    }

    /**
     * 运行sql语句。
     * @author xuankai
     * @param sql String sql语句
     * @return 返回语句是否正确,运行失败则返回false
     **/
    public final boolean executeSql(String sql) {
        Connection conn = openConnection();
        Statement stmt = null;
        try {
            stmt = conn != null ? conn.createStatement() : null;
            if(stmt == null) {
                throw new Exception();
            }
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("Mysql操作失败,语句为" + sql);
            closeConnection(conn, stmt);
            return false;
        }
        closeConnection(conn, stmt);
        return true;
    }

    /**
     * 运行sql语句获取一个或一组数据库对象的值。
     * @author xuankai
     * @param <T> 对象类型
     * @param clazz Class 返回的实例类型， 这个类需要有空参构造函数
     * @param sql String sql语句
     * @return ArrayList 查询得到的实例集合，如果不存在则返回空集合，运行失败则返回null
     */
    public final <T> ArrayList<T> query(Class<T> clazz, String sql) {
        ArrayList<T> list = new ArrayList<>();
        Connection conn = openConnection();
        Statement stmt = null;
        try {
            stmt = conn != null ? conn.createStatement() : null;
            if(stmt == null) {
                throw new Exception();
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs == null) {
                throw new Exception();
            }
            ResultSetMetaData rsmd = rs.getMetaData();

            T obj;
            for(int columns = rsmd.getColumnCount(); rs.next(); list.add(obj)) {
                obj = clazz.newInstance();
                for(int i = 1; i <= columns; i++) {
                    Field field = clazz.getDeclaredField(rsmd.getColumnName(i));
                    field.setAccessible(true);
                    field.set(obj, rs.getObject(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Mysql操作失败,语句为" + sql);
            closeConnection(conn, stmt);
            return null;
        }
        closeConnection(conn, stmt);
        return list;
    }

    /**
     * 执行数据库语句并获取第一个获得的值。
     * @author xuankai
     * @param <T> 获取的值的类型
     * @param sql String sql语句
     * @return T 查询得到的实例，如果表中不存在该数据则返回null
     */
    public final <T> T getValue(String sql) {
        Connection conn = openConnection();
        Statement stmt = null;
        T result = null;
        try {
            stmt = conn != null ? conn.createStatement() : null;
            if(stmt == null) {
                throw new Exception();
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs == null) {
                throw new Exception();
            }
            if(rs.next()) {
                @SuppressWarnings("unchecked")
                T res = (T) rs.getObject(1);
                result = res;
            }
        } catch (Exception e) {
            System.out.println("Mysql操作失败,语句为" + sql);
            closeConnection(conn, stmt);
            return null;
        }
        closeConnection(conn, stmt);
        return result;
    }

    /**
     * 执行数据库语句并获取第一个获得的值，无论如何将这个值作为字符串处理。
     * @author xuankai
     * @param sql String sql语句
     * @return String 查询得到的实例的字符串，如果表中不存在该数据则返回null
     */
    public final String getValueAsString(String sql) {
        Connection conn = openConnection();
        Statement stmt = null;
        String result = null;
        try {
            stmt = conn != null ? conn.createStatement() : null;
            if(stmt == null) {
                throw new Exception();
            }
            ResultSet rs = stmt.executeQuery(sql);
            if(rs == null) {
                throw new Exception();
            }
            if(rs.next()) {
                result = rs.getObject(1).toString();
            }
        } catch (Exception e) {
            System.out.println("Mysql操作失败,语句为" + sql);
            closeConnection(conn, stmt);
            return null;
        }
        closeConnection(conn, stmt);
        return result;
    }

    /**
     * 获取所有该数据库对象表中的值。
     * @author xuankai
     * @param <T> 对象类型
     * @param clazz Class 实例类型，这个类需要有空参构造函数
     * @return ArrayList 查询得到的实例集合，如果不存在则返回空集合，运行失败则返回null
     */
    public final <T> ArrayList<T> query(Class<T> clazz) {
        String sql = "select * from " + clazz.getSimpleName().toLowerCase(Locale.ROOT);
        return query(clazz, sql);
    }

    /**
     * 查询在sql中与obj的fieldName属性相同的对象。
     * 当fieldName为空时默认查询与obj主键相同的对象。
     * 注意，当fieldName不为空时不会主动查询主键，需要手动添加。
     * @author xuankai
     * @param <T> 对象类型
     * @param obj T 用于获取数据的对象
     * @param fieldName 查询的属性
     * @return 查询得到的实例集合，如果不存在则返回空集合，运行失败则返回null
     **/
    public final <T> ArrayList<T> query(T obj, String... fieldName) {
        StringBuilder sql = new StringBuilder("select * from " + obj.getClass().getSimpleName().toLowerCase(Locale.ROOT) + " where ");
        boolean firstAdd = true;

        if(fieldName.length == 0) {
            //根据主键查询
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(PrimaryKey.class)) {
                    if (firstAdd) {
                        firstAdd = false;
                    } else {
                        sql.append(" and ");
                    }
                    if (appendObjField(obj, sql, f)) return null;
                }
            }
        } else {
            //根据提供的field查询
            try {
                for(String name: fieldName) {
                    Field f = obj.getClass().getDeclaredField(name);
                    if (firstAdd) {
                        firstAdd = false;
                    } else {
                        sql.append(" and ");
                    }
                    if (appendObjField(obj, sql, f)) return null;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        @SuppressWarnings("unchecked")
        ArrayList<T> result = query((Class<T>) obj.getClass(), sql.toString());
        return result;
    }
}