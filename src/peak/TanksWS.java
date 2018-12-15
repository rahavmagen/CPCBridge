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

/**
 * Servlet implementation class
 */
public class TanksWS extends HttpServlet {
	/*
	 *  
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * The contract id - will be replaced with a parameter in the future
	 */
	private static final int theContractId  = 1 ;
	/* 
	 * JSON tags
	 */

	private static final int cTotalResultsCount = 24 ;
	private static final int cActualResultsCount = 3 ; /* Note: Now (+0) is part of the actual results section */
	
	/*
	 * Globals
	 */
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TanksWS() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		/*
		 * Logic:
		 *  This method will return the data for the Tanks Chart.
		 *  It will get three parameters via the path: Type (A, P) (e.g all, pressure zone) and value for type (if not A) and time value (date and time)
         * Will return 27 points -  3 actual 24 predicted (infact 25 predicted as the "now" is in both
 
		 *  
		 *  change log:
		 *  Date          Author     Reason
		 *  ===========   ========   ============================================
		 *  31-Sep-2017   Yoram      Initial Version
		 *  26-Nov-2017   Yoram      have min and max as data serials 
		 */

			String PathParams=request.getPathInfo() ;
			
			String pQueryType=null ; /* A - All, P - Presure Zone */ 
			String pQueryFilter=null ;
			String pBaseDate=null ; /* Format yyyy-mm-dd */
			String pBaseHour=null ; /* format hh:mi */
			
			String ResponseString="" ;



			if (PathParams==null || PathParams.equals("/"))  
				pQueryType=null ;
			else {
				pQueryType = PathParams.substring(1);
				if (pQueryType.indexOf("/")==-1)
                   pQueryFilter=null;
			else {
				if (pQueryType.substring(0,pQueryType.indexOf('/')).equals("A")) {
					pQueryFilter=pQueryType ; /// Take the whole path parameters from here on
					pQueryType = "A";
				}
				else {
				     pQueryFilter=pQueryType.substring(pQueryType.indexOf('/') + 1) ;
				     pQueryType=pQueryType.substring(0, pQueryType.indexOf('/')) ;
				}
				if (pQueryFilter.indexOf('/')==-1)
					pBaseDate=null ;
				else {
					pBaseDate=pQueryFilter.substring(pQueryFilter.indexOf('/') + 1) ;
					pQueryFilter=pQueryFilter.substring(0, pQueryFilter.indexOf('/')) ;
					if (pBaseDate.indexOf('/')==-1)
						pBaseHour=null ;
					else {
						pBaseHour=pBaseDate.substring(pBaseDate.indexOf('/') + 1) ;
						pBaseDate=pBaseDate.substring(0, pBaseDate.indexOf('/')) ;
					}
				}
			}
			// Check parameters //
			if (pQueryType==null || pQueryFilter == null || pBaseDate==null || pBaseHour==null ) {
				System.out.println("Error in get path variables "+pQueryType + " " + pQueryFilter + " " +pBaseDate + " " + pBaseHour );
				 response.getWriter().println("{ result: \"Error in get path variables "+pQueryType + " " + pQueryFilter + " " +pBaseDate + " " + pBaseHour + "  \" }") ;
				return ;
				
			}
			} 			
			// Fix hour. We get only the hour and we need hh:mm format
			
			pBaseHour = pBaseHour + ":00" ;
/* Nullify the counters */
  
	        String ActualResults = getActualFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ; // Also fills the min and max  
	        /* Prepare the result string */
	        ResponseString="{ " ;
	        /* Get Data  */
	        
/*
  	        ResponseString += " \"minimum\" : " + getMinFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ;
	        ResponseString += " , " ;
	      	ResponseString += " \"maximum\" : " + getMaxFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ;
	        ResponseString += " , " ;
*/
	        ResponseString += " \"datagraph\": [ " ;
	          ResponseString += " { \"label\": \"actual\" , " ;
	          ResponseString += " \"data\" : " + ActualResults ; 
	          ResponseString += " } " ;
	          ResponseString += " , " ;
	          ResponseString += " { \"label\": \"predict\" , " ;
	          ResponseString += " \"data\" : " + getPredictFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ;
	          ResponseString += " } " ;
	          ResponseString += " , " ;
	          ResponseString += " { \"label\": \"minimum\" , " ;
	          ResponseString += " \"data\" : " + fillVector(getMinFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour)) ;
	          ResponseString += " } " ;
	          ResponseString += " , " ;
	          ResponseString += " { \"label\": \"maximum\" , " ;
	          ResponseString += " \"data\" : " + fillVector(getMaxFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour)) ;
	          ResponseString += " } " ;
           ResponseString += " ] " ;

	         // Close the whole response
	         ResponseString += " }";
	         
				// Set response header 
		 		response.setContentType("application/json;charset=UTF-8");

			 response.getWriter().println(ResponseString );

		}
	
	private String fillVector(String theVal) {
		String tmpStr ;
		
		tmpStr = " [ " ;

		for (int i = (1 - cActualResultsCount)  ; i <= cTotalResultsCount ; i ++) {
			if (i > (1 - cActualResultsCount)) {
				tmpStr += "," ;
			}
			tmpStr += theVal ;
		} 
		tmpStr += " ] " ;
		return (tmpStr) ;
	}
	private String getMinFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
		/* Will be called three times with mode for now,yesterday, and weekly summary */
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

    String tmpResponseString = "" ;

	try {
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get driver class\" }" );
		return ("{ result: \"Error in get driver class\" }");
	}
	conn = ConnectionUtil.createDbConnection();
	
    if (conn==null) {
    	System.out.println("{ result: \"Connection is null\" }" );
		 return("{ result: \"Connection is null\" }") ;

    }

    try {
		stmt = conn.createStatement();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in create statement\" }" );
		 return("{ result: \"Error in create statement\" }") ;
	}
    
    QryString = null ;
	// Build the result prefix
    tmpResponseString="" ;
	        try {
	        	    
	        	    QryString= "select sum(operational_min_value) min_value from scada_meters" ;
	        	    QryString += " where meter_type in (select meter_type from meter_types where kind = 'V') " ; 
	        	    QryString += " and element_id in (select element_id from elements where classification = 'T'" ;
    		        if (pQueryType.equals("P")) {
    		        	QryString += " and pz_id = " + pQueryFilter ;
    		        }
    		        if (pQueryType.equals("T")) {
    		        	QryString += " and element_id = " + pQueryFilter ;
    		        }
    	            QryString +=" ) " ;
    	                	 
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in calculated min sql\" }" );
		 return("{ result: \"Error in calculated min sql: " + QryString + "\" }") ;
	}
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("min_value") == null) {
				tmpResponseString += "\"null\"" ;
			}
			
			else {
				tmpResponseString += "\"" + result.getString("min_value") + "\"";
			}
		}
		else /* No data found */ {
			tmpResponseString += "\"null\"" ;
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
    	
     try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
     
		return (tmpResponseString) ;
	}


	private String getMaxFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
		/* Will be called three times with mode for now,yesterday, and weekly summary */
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

    String tmpResponseString = "" ;

	try {
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get driver class\" }" );
		return ("{ result: \"Error in get driver class\" }");
	}
	conn = ConnectionUtil.createDbConnection();
	
    if (conn==null) {
    	System.out.println("{ result: \"Connection is null\" }" );
		 return("{ result: \"Connection is null\" }") ;

    }

    try {
		stmt = conn.createStatement();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in create statement\" }" );
		 return("{ result: \"Error in create statement\" }") ;
	}
    
    QryString = null ;
	// Build the result prefix
    tmpResponseString="" ;
	        try {
	        	    
	        	    QryString= "select sum(operational_max_value) max_value from scada_meters" ;
	        	    QryString += " where meter_type in (select meter_type from meter_types where kind = 'V') " ; 
    		        QryString += " and element_id in (select element_id from elements where classification = 'T'" ;
    		        if (pQueryType.equals("P")) {
    		        	QryString += " and pz_id = " + pQueryFilter ;
    		        }
    		        if (pQueryType.equals("T")) {
    		        	QryString += " and element_id = " + pQueryFilter ;
    		        }
    	            QryString +=" ) " ;
    	                	 
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in calculated max sql\" }" );
		 return("{ result: \"Error in calculated max sql: " + QryString + "\" }") ;
	}
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("max_value") == null) {
				tmpResponseString += "\"null\"" ;
			}
			
			else {
				tmpResponseString += "\"" + result.getString("max_value") + "\"";
			}
		}
		else /* No data found */ {
			tmpResponseString += "\"null\"" ;
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
    	
     try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
     
		return (tmpResponseString) ;
	}

	private String getActualFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

    String tmpResponseString = "" ;

	try {
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get driver class\" }" );
		return ("{ result: \"Error in get driver class\" }");
	}
    
	conn = ConnectionUtil.createDbConnection();
	
    if (conn==null) {
    	System.out.println("{ result: \"Connection is null\" }" );
		 return("{ result: \"Connection is null\" }") ;

    }

    try {
		stmt = conn.createStatement();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in create statement\" }" );
		 return("{ result: \"Error in create statement\" }") ;
	}
    
    QryString = null ;
	// Build the result prefix
    tmpResponseString=" [ " ;
    	for (int offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    QryString = "select format(sum(value),'0.#') as total_sum from Normalized_Events " ;
	        	    QryString += " where time_value = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)) "  ;
	        	    QryString += " and time_value <= sysdatetime() "  ; // only historical values (in fact. There can not be future data in this table - but we leave the check just to be 100% sure
	        	    QryString += " and meter_id in (select meter_id from scada_meters " ;
	        	    QryString += " where meter_type in (select meter_type from meter_types where kind = 'V')" ; //Count only Volume
	        	    QryString += " and element_id in (select element_id from elements where classification = 'T'" ;
	            	if (pQueryType.equals("P")) {
	            		QryString += " and pz_id = " + pQueryFilter ;
	            	}
	            	if (pQueryType.equals("T")) {
	            		QryString += " and element_id = " + pQueryFilter ;
	            	}
	            	/* Note: If type is A (all) - do not restrict at all */
	        	    QryString += ")" ; // Close the sub query for element id's
	        	    QryString += ")" ; // Close the sub query for meter id's
		            result = stmt.executeQuery(QryString);
	        } catch (SQLException e) {
		          e.printStackTrace();
		          System.out.println("{ result: \"Error in actual tank plan sql\" }" );
		          return("{ result: \"Error in actaual tank plan sql: " + QryString + "\" }") ;
	              }
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("total_sum") == null)
				tmpResponseString += "\"null\"" ; 
			else {
				// Add to json results
				tmpResponseString += "\"" + result.getString("total_sum") +"\"" ;
			}
		}
		else /* No data found */
			tmpResponseString += "\"null\"" ;
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error "+e.getMessage()  +" in fetch for sql:\" " + QryString + "\" } ") ;
		}
        if (offSet < cTotalResultsCount) {
     	   tmpResponseString += ", " ;
        }
    	}
    	tmpResponseString += " ] " ;
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
    	
			return (tmpResponseString) ;
	}
	
	private String getPredictFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

    String tmpResponseString = "" ;

	try {
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get driver class\" }" );
		return ("{ result: \"Error in get driver class\" }");
	}
    
	conn = ConnectionUtil.createDbConnection();
    
    if (conn==null) {
    	System.out.println("{ result: \"Connection is null\" }" );
		 return("{ result: \"Connection is null\" }") ;

    }

    try {
		stmt = conn.createStatement();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in create statement\" }" );
		 return("{ result: \"Error in create statement\" }") ;
	}
    
    QryString = null ;
	// Build the result prefix
    tmpResponseString=" [ " ;
    	for (int offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    QryString = "select format(sum(value),'0.#') as total_sum from Planned_Events " ;
	        	    QryString += " where time_value = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)) "  ;
	        	    QryString += " and time_value >= dateadd(mi,-59,sysdatetime()) "  ; // only futue values
	        	    QryString += " and meter_id in (select meter_id from scada_meters " ;
	        	    QryString += " where meter_type in (select meter_type from meter_types where kind = 'V')" ; //Count only Volume
	        	    QryString += " and element_id in (select element_id from elements where classification = 'T'" ;
	            	 if (pQueryType.equals("P")) {
	            		QryString += " and pz_id = " + pQueryFilter ;
	            	}
	            	 if (pQueryType.equals("T")) {
	            		QryString += " and element_id = " + pQueryFilter ;
	            	}
	            	/* Note: If type is A (all) - do not restrict at all */
	        	    QryString += ")" ; // Close the sub query for element id's
	        	    QryString += ")" ; // Close the sub query for meter id's
		            result = stmt.executeQuery(QryString);
	        } catch (SQLException e) {
		          e.printStackTrace();
		          System.out.println("{ result: \"Error in approved power plan sql\" }" );
		          return("{ result: \"Error in approved power plan sql: " + QryString + "\" }") ;
	              }
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("total_sum") == null)
				tmpResponseString += "\"null\"" ; 
			else {
				// Add to json results
				tmpResponseString += "\"" + result.getString("total_sum") +"\"" ;
			}
		}
		else /* No data found */
			tmpResponseString += "\"null\"" ;
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error "+e.getMessage()  +" in fetch for sql:\" " + QryString + "\" } ") ;
		}
        if (offSet < cTotalResultsCount) {
     	   tmpResponseString += ", " ;
        }
    	}
    	tmpResponseString += " ] " ;
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
			return (tmpResponseString) ;
	}

	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doGet(request, response);
		}
		
}
