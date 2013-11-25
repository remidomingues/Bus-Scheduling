/**
 * File : MatrixDB.java
 *
 * Created on May 25, 2012, 1:25:10 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package dataLoader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import locationsData.Location;

/** 
 * 
 */
public class DatabaseManager
{
	/** The unique instance of this class */
	private static DatabaseManager instance;
	
	/** The DAO used to interact with database */
	protected DAO dao;


	/** The location table name */
	protected static final String TABLE_LOCATION = "location";

	/** Location id column */
	protected static final String ID_LOCATION = "IdLocation";

	/** Location address column */
	protected static final String ADDRESS = "address";

	/** Location latitude column */
	protected static final String LATITUDE = "latitude";

	/** Location longitude column */
	protected static final String LONGITUDE = "longitude";

	/** Location additional informations column */
	private static final String INFORMATIONS = "Informations";


	/** Path data table name */
	protected static final String TABLE_DATA = "pathdata";

	/** PathData origin location id */
	protected static final String ID_ORIGIN_LOCATION = "IdOrigin";

	/** PathData destination location id */
	protected static final String ID_DESTINATION_LOCATION = "IdDestination";

	/** PathData distance column */
	protected static final String DISTANCE = "distance";
	
	/** PathData duration column */
	protected static final String DURATION = "duration";
	
	
	/** If the value isn't set */
	public static final int UNSET_VALUE = -1;
	
	/**
	 * Private constructor
	 * @throws ClassNotFoundException If the JDBC driver can't be found
	 * @throws SQLException If the database connection return an error 
	 */
	private DatabaseManager() throws ClassNotFoundException, SQLException
	{
		dao = DAO.getInstance();
	}
	
	/**
	 * Return the unique DatabaseManager instance 
	 * @return The unique DatabaseManager instance 
	 * @throws ClassNotFoundException If the JDBC driver can't be found
	 * @throws SQLException If the database connection return an error 
	 */
	public static DatabaseManager getInstance() throws ClassNotFoundException, SQLException
	{
		if(instance == null)
			instance = new DatabaseManager();
		if(instance.dao.connection.isClosed())
			instance.dao.connect();
		return instance;
	}

	/**
	 * Insert a path data in the path data table
	 * @param origin The origin location id
	 * @param destination The destination location id
	 * @param distance The distance between these locations
	 * @param duration The duration between these locations
	 * @throws SQLException If the request return an error
	 */
	public void insertPathData(Location origin, Location destination, int distance, int duration) throws SQLException
	{
		// INSERT INTO PathData VALUES (?, ?, ?, ?)
		StringBuilder query = new StringBuilder().append("INSERT INTO ").append(TABLE_DATA).append(" VALUES ").append(" (?, ?, ?, ?)");
		PreparedStatement ps = dao.prepareStatement(query.toString());
        ps.setInt(1, origin.getId());
        ps.setInt(2, destination.getId());
        ps.setInt(3, distance);
        ps.setInt(4, duration);
        dao.update(ps);
        ps.close();
	}

	/**
	 * Insert a location in the location table
	 * @param loc The location to insert
	 * @throws SQLException If the request return an error
	 */
	public void insertLocation(Location loc) throws SQLException
	{
		// INSERT INTO Location (Address, Latitude, Longitude, Informations) VALUES (?, ?, ?, ?)
		StringBuilder query = new StringBuilder();
		// INSERT TO
		query.append("INSERT INTO ").append(TABLE_LOCATION).append(" (").append(ADDRESS).append(", ")
			.append(LATITUDE).append(", ").append(LONGITUDE).append(", ").append(INFORMATIONS).append(") ");
		// VALUES
		query.append("VALUES (?, ?, ?, ?)");
		
		PreparedStatement ps = dao.prepareStatement(query.toString());
		ps.setString(1, loc.getAddress());
		ps.setFloat(2, loc.getLatitude());
		ps.setFloat(3, loc.getLongitude());
		if(loc.getAdditionalInformations() != null)
			ps.setString(4, loc.getAdditionalInformations());
		else
			ps.setString(4, "");
		
		dao.update(ps);

        ps.close();
	}
	
	/**
	 * Return the distance or duration between two location
	 * @param originId The first location id
	 * @param destinationId The second location id
	 * @param data The data asked (distance or duration)
	 * @return The data between the two locations
	 * @throws SQLException If the request return an error
	 */
	private int getPathData(int originId, int destinationId, String data) throws SQLException
	{
		//SELECT x FROM PathData WHERE IdOrigin = ? AND IdDestination = ?;
		StringBuilder query = new StringBuilder();
		//SELECT
		query.append("SELECT ").append(data).append(" FROM ").append(TABLE_DATA)
			.append(" WHERE ").append(ID_ORIGIN_LOCATION).append(" = ? AND ")
			.append(ID_DESTINATION_LOCATION).append(" = ?");

		PreparedStatement ps = dao.prepareStatement(query.toString());
		ps.setInt(1, originId);
		ps.setInt(2, destinationId);
		ResultSet rs = dao.query(ps);

		int value = UNSET_VALUE;
		while (rs.next())
		{
			value = rs.getInt(data);
		}
		rs.close();
        ps.close();
		return value;
	}
	
	/**
	 * Return the duration between two locations
	 * @param originId The first location id
	 * @param destinationId The second location id
	 * @return The duration between two locations
	 * @throws SQLException If the request return an error
	 */
	public int getDuration(int originId, int destinationId) throws SQLException
	{
		return getPathData(originId, destinationId, DURATION);
	}
	
	/**
	 * Return the distance between two locations
	 * @param originId The first location id
	 * @param destinationId The second location id
	 * @return The distance between two locations
	 * @throws SQLException If the request return an error
	 */
	public int getDistance(int originId, int destinationId) throws SQLException
	{
		return getPathData(originId, destinationId, DISTANCE);
	}
	
	/**
	 * Return true if a path data exists between the two locations, false else
	 * @param l1 The first location
	 * @param l2 The second location
	 * @return true if it exists, false else
	 * @throws SQLException If the request return an error
	 */
	public boolean existPathData(Location l1, Location l2) throws SQLException
	{
		int count = 0;
		//SELECT count(*) FROM PathData WHERE IdOrigin = ? AND IdDestination = ?;
		StringBuilder query = new StringBuilder();
		//SELECT
		query.append("SELECT count(*) FROM ").append(TABLE_DATA)
			.append(" WHERE ").append(ID_ORIGIN_LOCATION).append(" = ? AND ")
			.append(ID_DESTINATION_LOCATION).append(" = ?");

		PreparedStatement ps = dao.prepareStatement(query.toString());
		ps.setInt(1, l1.getId());
		ps.setInt(2, l2.getId());
		ResultSet rs = dao.query(ps);

		while (rs.next())
		{
			count = rs.getInt("count(*)");
		}
		rs.close();
        ps.close();
		if(count == 0)
			return false;
		else if(count == 1)
			return true;
		System.err.println("Uma incoerência foi detectada na base de dados." +
				" Vários itinerários estão disponíveis entre dois lugares.");
		return true;
	}
	
	/**
	 * Return the id of a location based on its latitude and longitude
	 * @param loc The location to ask for
	 * @return The location id
	 * @throws SQLException If the request return an error
	 */
	public int getLocationId(Location loc) throws SQLException
    {
		//SELECT IdLocation FROM Location WHERE Latitude = ? AND Longitude = ?"
        PreparedStatement ps = dao.prepareStatement(new StringBuilder()
        	.append("SELECT ").append(ID_LOCATION).append(" FROM ")
        	.append(TABLE_LOCATION).append(" WHERE ").append(LATITUDE).append(" = ?")
        	.append(" AND ").append(LONGITUDE).append(" = ?").toString());
        ps.setFloat(1, loc.getLatitude());
        ps.setFloat(2, loc.getLongitude());
        ResultSet rs = dao.query(ps);
        
        int id = UNSET_VALUE;
        while(rs.next())
        {
    		id = rs.getInt("IdLocation");
        }
		rs.close();
        ps.close();
        return id;
    }
	
	/**
	 * Return the geographic coordinates of a location
	 * @param l The location to ask for
	 * @return The geographic coordinates
	 * @throws SQLException If the request return an error
	 */
	public float[] getLocationCoordinates(Location l) throws SQLException
	{
		//SELECT Latitude, Longitude FROM Location WHERE IdLocation = ?
		StringBuilder query = new StringBuilder();
		//SELECT
		query.append("SELECT ").append(LATITUDE).append(", ").append(LONGITUDE).append(" FROM ")
			.append(TABLE_LOCATION).append(" WHERE ").append(ID_LOCATION).append(" = ?");

		PreparedStatement ps = dao.prepareStatement(query.toString());
		ps.setInt(1, l.getId());
		ResultSet rs = dao.query(ps);

		float[] coord = new float[2];
		while (rs.next())
		{
			coord[0] = rs.getFloat(LATITUDE);
			coord[1] = rs.getFloat(LONGITUDE);
		}
		rs.close();
        ps.close();
		return coord;
	}
	
	/**
	 * Remove every path data relative to a location
	 * @param l The location specified
	 * @throws SQLException If the request return an error
	 */
	public void removePathsData(Location l) throws SQLException
	{
		// DELETE FROM PathData WHERE IdOrigin = ? OR IdDestination = ?
		StringBuilder query = new StringBuilder().append("DELETE FROM ").append(TABLE_DATA).append(" WHERE ").append(ID_ORIGIN_LOCATION)
				.append(" OR ").append(ID_DESTINATION_LOCATION).append(" = ?");
		PreparedStatement ps = dao.prepareStatement(query.toString());
        ps.setInt(1, l.getId());
        ps.setInt(1, l.getId());
        dao.update(ps);
        ps.close();
	}
	
	/**
	 * Delete a location
	 * @param l The location to delete
	 * @throws SQLException If the request return an error
	 */
	public void removeLocation(Location l) throws SQLException
	{
		// DELETE FROM Location WHERE IdLocation = ?
		StringBuilder query = new StringBuilder().append("DELETE FROM ").append(TABLE_LOCATION).append(" WHERE ").append(ID_LOCATION)
				.append(" = ?");
		PreparedStatement ps = dao.prepareStatement(query.toString());
        ps.setInt(1, l.getId());
        dao.update(ps);
        ps.close();
	}
	
	/**
	 * Update the address of a location
	 * @param id Location id
	 * @param address Address to update
	 * @throws SQLException If the request return an error
	 */
	public void updateLocationAddress(int id, String address) throws SQLException
	{
		// UPDATE Location SET Address = ? WHERE IdLocation = ?
		StringBuilder query = new StringBuilder().append("UPDATE ").append(TABLE_LOCATION).append(" SET ").append(ADDRESS)
				.append(" = ? WHERE ").append(ID_LOCATION).append(" = ?");
		PreparedStatement ps = dao.prepareStatement(query.toString());
        ps.setString(1, address);
        ps.setInt(2, id);
        dao.update(ps);
        ps.close();
	}

	/**
	 * Close the database connection
	 * @throws SQLException If the closing returns an error
	 */
    public void close() throws SQLException
    {
        dao.close();
    }
}
