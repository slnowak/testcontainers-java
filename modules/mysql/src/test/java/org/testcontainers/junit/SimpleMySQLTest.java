package org.testcontainers.junit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;


/**
 * @author richardnorth
 */
public class SimpleMySQLTest {

    @ClassRule
    public static MySQLContainer mysql = new MySQLContainer();

    @ClassRule
    public static MySQLContainer mysqlOldVersion = new MySQLContainer("mysql:5.5");

    @ClassRule
    public static MySQLContainer mysqlCustomConfig = new MySQLContainer("mysql:5.6")
                                                            .withConfigurationOverride("somepath/mysql_conf_override");

    @Test
    public void testSimple() throws SQLException {
        ResultSet resultSet = performQuery(mysql, "SELECT 1");
        int resultSetInt = resultSet.getInt(1);

        assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
    }

    @Test
    public void testSpecificVersion() throws SQLException {
        ResultSet resultSet = performQuery(mysqlOldVersion, "SELECT VERSION()");
        String resultSetString = resultSet.getString(1);

        assertTrue("The database version can be set using a container rule parameter", resultSetString.startsWith("5.5"));
    }

    @Test
    public void testMySQLWithCustomIniFile() throws SQLException {
        ResultSet resultSet = performQuery(mysqlCustomConfig, "SELECT @@GLOBAL.innodb_file_format");
        String result = resultSet.getString(1);

        assertEquals("The InnoDB file format has been set by the ini file content", "Barracuda", result);
    }

    @NotNull
    protected ResultSet performQuery(MySQLContainer containerRule, String sql) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(containerRule.getJdbcUrl());
        hikariConfig.setUsername(containerRule.getUsername());
        hikariConfig.setPassword(containerRule.getPassword());

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        return resultSet;
    }
}
