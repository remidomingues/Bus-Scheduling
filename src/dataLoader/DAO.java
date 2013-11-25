/**
 * File : DAO.java
 *
 * Created on May 29, 2012, 8:51:31 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package dataLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Database Access Object, used to connect and do query and updates to 
 */
public class DAO
{
	/** The database connection */
	protected Connection connection;

	/** Database url */
	protected String database = "localhost:3306/gidion";

	/** User login */
	protected String login = "gidion";

	/** User password */
	protected String password = "gi7dw4";
	
	/** The unique instance of this class */
	private static DAO instance;
	
	/**
	 * Private constructor
	 * @throws ClassNotFoundException If the JDBC driver can't be found
	 * @throws SQLException If the database connection return an error 
	 */
	private DAO() throws ClassNotFoundException, SQLException
	{
		connect();
	}
	
	/**
	 * Create a connection to the database
	 * @throws ClassNotFoundException If the JDBC driver can't be found
	 * @throws SQLException If the database connection return an error 
	 */
	public void connect() throws ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://" + database;
		connection = DriverManager.getConnection(url, login, password);
	}
	
	/**
	 * Return the unique DAO instance 
	 * @return The unique DAO instance 
	 * @throws ClassNotFoundException If the JDBC driver can't be found
	 * @throws SQLException If the database connection return an error 
	 */
	public static DAO getInstance() throws ClassNotFoundException, SQLException
	{
		if(instance == null)
			instance = new DAO();
		return instance;
	}

	/**
	 * Execute an update request on database
	 * @param ps The prepared statement to execute
	 * @throws SQLException If the request returns an error
	 */
	public void update(PreparedStatement ps) throws SQLException
    {
        ps.executeUpdate();
    }

	/**
	 * Execute an query request on database
	 * @param ps The prepared statement to execute
	 * @return The request result
	 * @throws SQLException If the request returns an error
	 */
    public ResultSet query(PreparedStatement ps) throws SQLException
    {
        return ps.executeQuery();
    }

	/**
	 * Close the database connection
	 * @throws SQLException If the closing returns an error
	 */
    public void close() throws SQLException
    {
        connection.close();
    }

	/**
	 * Return a prepared statement created from a string request
	 * @param s The request to prepare
	 * @return The prepared statement
	 * @throws SQLException If the prepare statement creation returns an error
	 */
    public PreparedStatement prepareStatement(String s) throws SQLException
    {
        return connection.prepareStatement(s);
    }
}
