package peak;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Servlet implementation class
 */
public class PresureZoneWS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PresureZoneWS() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String QryString;
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet result = null;

		String TempResponseString="" ;
		String ResponseString="" ;

		try {
			 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in get driver class\" }" );
			return ;
		}

		conn = ConnectionUtil.createDbConnection();
        if (conn==null) {
  		    response.getWriter().println("{ result: \"Connection is null\" }" );
			return ;

        }

        try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in create statement\" }" );
			return ;
		}
        try {
        	QryString= "select pz_id,presure_zone_name,presure_zone_hebrew_name from [dbo].[presure_zones]" ;
			result = stmt.executeQuery(QryString);
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in sql\" }" );
			return ;
		}
		
         try {
			while (result.next()) {

  				 TempResponseString= 
						  " { " 
						 + "\"id\" : "
						 + result.getString(1)
						 + " , "
						 + "\"name\" : "
						 + "\"" + result.getString(2) + "\""
						 + " , "
						 + "\"hebrewName\" : "
						 + "\"" + result.getString(3) + "\""
						 + " , "
						 + "\"class\" : "
						 + "\"unselected\""
						 + " } " ;
					if (ResponseString==null || ResponseString.equals(""))
						ResponseString=TempResponseString ;
				else
					ResponseString=ResponseString+" , " +TempResponseString ; 
			 }
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in getting result set from sql\" }" );
			return ;
		}
         ResponseString="[ " + ResponseString + " ]";
         // close the connection, resultset, and the statement
         try {
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in close result set\" }" );
			return ;
		}
         try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in close statement\" }" );
			return ;
		}
         try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in close connection\" }" );
			return ;
		}
		
			// Set response header 
	 		response.setContentType("application/json;charset=UTF-8");

		   response.getWriter().println(ResponseString);
		
		

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
