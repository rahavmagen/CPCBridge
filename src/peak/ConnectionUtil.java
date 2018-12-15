package peak;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * TODO Put here a description of what this class does.
 *
 * @author app_rahavm.
 *         Created Dec 20, 2017.
 */
public class ConnectionUtil {
	
	public static Connection createDbConnection()
	{
		DataSource ds= null;
		try {
			Context init = new InitialContext() ;
			Context Context = (Context) init.lookup("java:/comp/env/");
			ds = (DataSource) Context.lookup("jdbc/peakdb");
			System.out.println("in createDbConnection");
			Connection tmpcon = ds.getConnection();
			
//			return ds.getConnection() ;
			return tmpcon;
		} 
		catch (NamingException  |SQLException  e) 
		{

			e.printStackTrace();
		}
		return null; 
		
	}

}
