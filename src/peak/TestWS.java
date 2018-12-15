package peak;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/*
 * Servlet implementation class
 */
public class TestWS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestWS() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String QryString;
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet result = null;

		String TempResponseString="" ;
		String ResponseString="" ;
		System.out.println("111111111111111");
		String PathParams=request.getPathInfo() ;
		
		String pQueryType=null ; /* null = all, P = by presure zone , T = specific tank */
		String pQueryFilter=null ; 

		if (PathParams==null || PathParams.equals("/"))  
			pQueryType=null ;
		else {
			pQueryType=PathParams.substring(1);
			if (pQueryType.indexOf('/')==-1)
				pQueryFilter=null ;
			else {
				pQueryFilter=pQueryType.substring(pQueryType.indexOf('/') + 1) ;
				pQueryType=pQueryType.substring(0, pQueryType.indexOf('/')) ;
			}
		}
		// Check parameters //
		if (pQueryType != null) {
		/* If 	pQueryType equals null it OK - we retreive all deamnds */
			if (!pQueryType.equals("P") && !pQueryType.equals("T")) {
				System.out.println("Error in get path variables : bad query type: " + pQueryType);
				response.getWriter().println("{ result: \"Error in get path variables : bad query type" + pQueryType  +"\" }") ;
			}
			if (pQueryFilter==null) {
				System.out.println("Error in get path variables : no query filter");
				response.getWriter().println("{ result: \"Error in get path variables : no query filter \" }") ;
			}
		}
/*		
		try {
			 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			 response.getWriter().println("{ result: \"Error in get driver class\" }" );
			return ;
		}
		*/
//	    try {
////			conn = DriverManager.getConnection("jdbc:sqlserver://localhost;DatabaseName=pddb","sa","Advantech1");
//	    	Context init = new InitialContext() ;
//	    	Context Context = (Context) init.lookup("java:/comp/env/");
//	    	DataSource ds = (DataSource) Context.lookup("jdbc/peakdb");
//	    	conn = ds.getConnection() ; 
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
        	QryString = "select element_id,element_name_english,Element_Name_Hebrew from elements " ;
        	QryString += " where classification = 'T'" ; // Only Tanks
        	/*
        	 * Now add the filter
        	 */
        	if (pQueryType != null) {
        	   if (pQueryType.equals("P")) 
        		   QryString += " and pz_id=" + pQueryFilter ;
        	   if (pQueryType.equals("T")) 
        		   QryString += " and element_id=" + pQueryFilter ;
        	}
        	/*
        	 * Now query the results
        	 */
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
