package peak;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import com.google.appengine.repackaged.org.joda.time.DateTime;

/*
 * Servlet implementation class
 */
public class WorkPlanWS extends HttpServlet {
	private static final int cTotalResultsCount = 24 ;
	private static final int cActualResultsCount = 3 ; /* Note: Now (+0) is part of the actual results section */
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WorkPlanWS() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Logic:
		 *  This method will return the data for the work plan (heat map) Chart.
		 *  It will get three parameters via the path: Type (A, P) (e.g all, pressure zone) and value for type (if not A) and time value (date and time)
         * Will return 24 points -  24 predicted
         * Will return the values for the approved plan
         *
		 *  
		 *  change log:
		 *  Date          Author     Reason
		 *  ===========   ========   ============================================
		 *  19-Nov-2017   Yoram      Initial Version
		 *  18-Dec-2017   Yoram      Change json to meet the highchart heatmap structure
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
				if (pQueryType.substring(0,pQueryType.indexOf('/')).equals("X")) {
					pQueryType = "X";
				}
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
			} 			
			// Fix hour. We get only the hour and we need hh:mm format
			
			pBaseHour = pBaseHour + ":00" ;
/* Nullify the counters */

			
	        /* Prepare the result string */
	        ResponseString=" [ " ;
	        /* Get Data  */
	        /* First the mock - for Sarah (26-Nov-2017) */
	        
	        if (pQueryType.equals("X"))
	           ResponseString += mockupData() ;
	        else {
	        
	        /* Real code resumed here */
	        
	        /* We retreive the rate first as we need the cost vector, but we will add it later (for the GUI to have it's layers OK */
	        
	             ResponseString += getAllDataFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ; //  Also fills Summaries vectors
	        }
	        
	         // Close the whole response
	         ResponseString += " ]";
	         
			
			// Set response header 
	         
	         
	 		response.setContentType("application/json;charset=UTF-8");
	 		
	 		// The actual response 
	 		
			 response.getWriter().println(ResponseString );
			 

		}
	
	private String getAllDataFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
		/* just check if there is a persure zone parameter or not and call GetData (once or as many times as needed */
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
    // Convert the date parameter
    
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Date theTs = null;
	try {
		theTs = df.parse(pBaseDate + " " + pBaseHour);
	} catch (ParseException e1) {
		e1.printStackTrace();
	} 
    QryString = null ;
	// Build the result prefix
	try {
	        	    
	    QryString= "select pz_id,presure_zone_name,presure_zone_hebrew_name from Presure_Zones " ;
	    if (pQueryType.equals("P")) {
	    	QryString +=" where pz_id = " + pQueryFilter ;
	    }
    	System.out.println("sql query is "+QryString);                	 
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get presure zones sql\" }" );
		 return("{ result: \"Error in get presure zones sql: " + QryString + "\" }") ;
	}
    
    // get the data
     try {
		while (result.next()) {
			if (!tmpResponseString.equals("")) {
				tmpResponseString += "," ;
			}
			if (result.getString("pz_id") == null) {
				tmpResponseString += "\"null\"" ;
			}
			
			else {
				tmpResponseString += "{" ;
				tmpResponseString += "\"label\" : \"" + result.getString("presure_zone_hebrew_name") + "\"" ;
				  tmpResponseString += " , ";
				  tmpResponseString += " \"wells\" : ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"W",theTs)  ;
				tmpResponseString += " , ";
				tmpResponseString += " \"boosters\" : ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"B",theTs)  ;
				tmpResponseString += " , ";
				tmpResponseString += " \"valves\" : ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"V",theTs)  ;
				tmpResponseString += " } ";
			}
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch preasure zones\" " + e.getMessage()) ;
		}
     try 
     {
    	 conn.close();
     } catch 
     (SQLException exception) 
     {

    	 System.out.println("canot close connection");
    	 exception.printStackTrace();
     }
     return (tmpResponseString) ;

	}

	private String getClassDataFromDB (String pPzId,String pClassification,Date pTs) {

	    SimpleDateFormat df_sql = new SimpleDateFormat("yyyy-MM-dd HH:mm") ;
		
		String tmpResults = "" ;
		String tmpCategoriesResults = "" ;
		String tmpDataResults = "" ;
		
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet result = null;
		String QryString;
	    Statement stmt1 = null;
	    ResultSet result1 = null;
		String QryString1;
		
	    int elementIndex = 0 ; // Needed for the heatmap json element

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
			stmt1 = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in create statement\" }" );
			 return("{ result: \"Error in create statement\" }") ;
		}
	    
	    QryString = null ;
		
		
		Calendar c = Calendar.getInstance();
		
		
		tmpCategoriesResults = "[" ;
		tmpDataResults = "[" ;
		
		QryString = "select e.Element_Name_Hebrew as element_name,m.meter_id as meter_id" ;
		QryString += " from elements e " ;
		QryString += " inner join scada_meters m on (e.element_id = m.element_id and m.meter_type in (select meter_type from meter_types where kind = 'S'))" ;
		QryString += " where e.pz_id = " + pPzId ;
		QryString += " and e.classification = '" + pClassification +"'" ;
		
		try {
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get elements and statuses reading sql\" }" );
		 return("{ result: \"Error in get elements and statuses reading sql: " + QryString + "\"" + e.getMessage() +"}" ) ;
	}
    
    // get the data
     try {
		while (result.next()) {
// Add the name to the categories list
			if (tmpCategoriesResults.equals("[")) {
				tmpCategoriesResults += " \"" + result.getString("element_name") + "\"";
			}
			else { // Not the first element
				tmpCategoriesResults += " , \"" + result.getString("element_name") + "\"";
			}
// fill all values for this element in data
			for (int i = 1-cActualResultsCount ; i < cTotalResultsCount ; i++) {
			   c.setTime(pTs) ;
			   c.add(Calendar.HOUR,i);
/*
 * values are
 * 0 : actual close
 * 1 : actual open
 * 2 : planned close
 * 3 : planned start
 * 4 : forced closed
 * 5 : forced open 			   
 */
			   if (i <= 0) {
				   /** Actual **/
					QryString1 = "select case when value = 0 then 0 else 1 end as value " ;
					QryString1 += " from normalized_events " ;
					QryString1 += " where meter_id = " + result.getString("meter_id");
					QryString1 += " and time_value = '" +  df_sql.format(c.getTime()) + "'" ;
			   }
			   else {
				   /** Planned (or forced) **/
					QryString1 = "select case when value = 0 then 2 else 3 end as value " ;
					QryString1 += " from planned_events " ;
					QryString1 += " where meter_id = " + result.getString("meter_id");
					QryString1 += " and time_value = '" +  df_sql.format(c.getTime()) + "'" ;
					QryString1 += " and run_id = (select parameter_value from parameters where parameter_name = 'APPROVED_PLAN')" ;
					QryString1 += " and not exists (select 'Found constraint'" ;
					QryString1 += "                 from Constraints c" ; 
					QryString1 += "                 where meter_id = " + result.getString("meter_id"); 
					QryString1 += "                 and '" + df_sql.format(c.getTime()) + "' between c.from_timestamp and c.to_timestamp "; 
					QryString1 += "                )" ; 
					QryString1 += " union all " ; 
					QryString1 += "select case when forced_value = 0 then 4 else 5 end as value " ;
					QryString1 += " from constraints " ;
					QryString1 += " where meter_id = " + result.getString("meter_id");
					QryString1 += " and '" +   df_sql.format(c.getTime()) + "' between from_timestamp and to_timestamp ";
						
				}
				try {
				result1 = stmt1.executeQuery(QryString1);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("{ result: \"Error in get a single cell value reading sql\" }" );
				 return("{ result: \"Error in get get a single cell value reading sql: " + QryString1 + "\" "+e.getMessage() +"}") ;
			}
		    
		    // get the data
		     try {
		    	 String tmpOneCell="" ;
		    	 
					if (result1.next()) { // Value found
						/* Note - We use i + (cActualResultsCount-1) just to have the X start from 0 (for the heatmap) */
							tmpOneCell = "[ " + Integer.toString(i + (cActualResultsCount-1)) + "," + Integer.toString(elementIndex) + "," + result1.getString("value") + "]"; 
						}
						else {
							tmpOneCell = "[ " + Integer.toString(i+ (cActualResultsCount-1)) + "," + Integer.toString(elementIndex) + "," + "0" + "]"; // An open question: what should we really return here  
						}
					if (tmpDataResults.equals("[")) {
						tmpDataResults += tmpOneCell ;  
					}
					else {
					  tmpDataResults += "," +  tmpOneCell ;
					}
		    	 
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch a single cell value\" " + e.getMessage()) ;
		}
	    } // end of loop over time stamps for a single element
	    elementIndex++ ;
			
		} // end of looping over all elements
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in loop over all elements\" }" );
			 return("{ result: \"Error in loop over all elements: " + QryString + "\" "+e.getMessage()+" }") ;
		}
			

		tmpCategoriesResults += "]" ;
		tmpDataResults += "]" ;
		
		tmpResults += " { " ;
		tmpResults += " \"categories\" : " + tmpCategoriesResults;
		tmpResults += " , " ;
		tmpResults += " \"data\" : " + tmpDataResults;
		tmpResults += " } " ;
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
		return tmpResults ;		
	}

  String mockupData() {
	  String tmpStr = "" ;
	  
		
		
tmpStr +="	[{	" ;
tmpStr +="	             \"label\": \"àæåø ðéñåé 1\",	" ;
tmpStr +="	             \"wells\": {	" ;
tmpStr +="	                            \"categories\": [\"ëìá 1\", \"çúåì 1\", \"çúåì 2\"],	" ;
tmpStr +="	                            \"data\": [	" ;
tmpStr +="	                                          [0, 0, 0],	" ;
tmpStr +="	                                          [0, 1, 1],	" ;
tmpStr +="	                                          [0, 2, 0],	" ;
tmpStr +="	                                          [1, 0, 3],	" ;
tmpStr +="	                                          [1, 1, 2],	" ;
tmpStr +="	                                          [1, 2, 4],	" ;
tmpStr +="	                                          [2, 0, 5],	" ;
tmpStr +="	                                          [2, 1, 2],	" ;
tmpStr +="	                                          [2, 2, 1],	" ;
tmpStr +="	                                          [3, 0, 4],	" ;
tmpStr +="	                                          [3, 1, 3],	" ;
tmpStr +="	                                          [3, 2, 3],	" ;
tmpStr +="	                                          [4, 0, 3],	" ;
tmpStr +="	                                          [4, 1, 5],	" ;
tmpStr +="	                                          [4, 2, 2],	" ;
tmpStr +="	                                          [5, 0, 4],	" ;
tmpStr +="	                                          [5, 1, 1],	" ;
tmpStr +="	                                          [5, 2, 3],	" ;
tmpStr +="	                                          [6, 0, 3],	" ;
tmpStr +="	                                          [6, 1, 4],	" ;
tmpStr +="	                                          [6, 2, 2],	" ;
tmpStr +="	                                          [7, 0, 5],	" ;
tmpStr +="	                                          [7, 1, 1],	" ;
tmpStr +="	                                          [7, 2, 2],	" ;
tmpStr +="	                                          [8, 0, 1],	" ;
tmpStr +="	                                          [8, 1, 2],	" ;
tmpStr +="	                                          [8, 2, 2],	" ;
tmpStr +="	                                          [9, 0, 3],	" ;
tmpStr +="	                                          [9, 1, 2],	" ;
tmpStr +="	                                          [9, 2, 1],	" ;
tmpStr +="	                                          [10, 0, 3],	" ;
tmpStr +="	                                          [10, 1, 2],	" ;
tmpStr +="	                                          [10, 2, 1],	" ;
tmpStr +="	                                          [11, 0, 3],	" ;
tmpStr +="	                                          [11, 1, 2],	" ;
tmpStr +="	                                          [11, 2, 4],	" ;
tmpStr +="	                                          [12, 0, 5],	" ;
tmpStr +="	                                          [12, 1, 2],	" ;
tmpStr +="	                                          [12, 2, 1],	" ;
tmpStr +="	                                          [13, 0, 4],	" ;
tmpStr +="	                                          [13, 1, 3],	" ;
tmpStr +="	                                          [13, 2, 3],	" ;
tmpStr +="	                                          [14, 0, 3],	" ;
tmpStr +="	                                          [14, 1, 5],	" ;
tmpStr +="	                                          [14, 2, 2],	" ;
tmpStr +="	                                          [15, 0, 4],	" ;
tmpStr +="	                                          [15, 1, 1],	" ;
tmpStr +="	                                          [15, 2, 3],	" ;
tmpStr +="	                                          [16, 0, 3],	" ;
tmpStr +="	                                          [16, 1, 4],	" ;
tmpStr +="	                                          [16, 2, 2],	" ;
tmpStr +="	                                          [17, 0, 5],	" ;
tmpStr +="	                                          [17, 1, 1],	" ;
tmpStr +="	                                          [17, 2, 2],	" ;
tmpStr +="	                                          [18, 0, 1],	" ;
tmpStr +="	                                          [18, 1, 2],	" ;
tmpStr +="	                                          [18, 2, 2],	" ;
tmpStr +="	                                          [19, 0, 3],	" ;
tmpStr +="	                                          [19, 1, 2],	" ;
tmpStr +="	                                          [19, 2, 1],	" ;
tmpStr +="	                                          [20, 0, 3],	" ;
tmpStr +="	                                          [20, 1, 2],	" ;
tmpStr +="	                                          [20, 2, 1],	" ;
tmpStr +="	                                          [21, 0, 3],	" ;
tmpStr +="	                                          [21, 1, 2],	" ;
tmpStr +="	                                          [21, 2, 4],	" ;
tmpStr +="	                                          [22, 0, 5],	" ;
tmpStr +="	                                          [22, 1, 2],	" ;
tmpStr +="	                                          [22, 2, 1],	" ;
tmpStr +="	                                          [23, 0, 4],	" ;
tmpStr +="	                                          [23, 1, 3],	" ;
tmpStr +="	                                          [23, 2, 3],	" ;
tmpStr +="	                                          [24, 0, 3],	" ;
tmpStr +="	                                          [24, 1, 5],	" ;
tmpStr +="	                                          [24, 2, 2],	" ;
tmpStr +="	                                          [25, 0, 4],	" ;
tmpStr +="	                                          [25, 1, 1],	" ;
tmpStr +="	                                          [25, 2, 3],	" ;
tmpStr +="	                                          [26, 0, 3],	" ;
tmpStr +="	                                          [26, 1, 4],	" ;
tmpStr +="	                                          [26, 2, 2]	" ;
tmpStr +="	                            ]	" ;
tmpStr +="	             },	" ;
tmpStr +="		" ;
tmpStr +="	             \"boosters\": {	" ;
tmpStr +="	                            \"categories\": [\"âáéðä éçéãä 1\", \"âáéðä éçéãä 2\", \"úçðä éôéú éçéãä 1\", \"úçðä éôéú éçéãä 2\", \"ðùø 2 ìá÷òåú éçéãä 1\", \"òëáø éçéãä 2\", \"òëáø éçéãä 3\"],	" ;
tmpStr +="	                            \"data\": [	" ;
tmpStr +="	                                          [0, 0, 0],	" ;
tmpStr +="	                                          [0, 1, 1],	" ;
tmpStr +="	                                          [0, 2, 0],	" ;
tmpStr +="	                                          [0, 3, 0],	" ;
tmpStr +="	                                          [0, 4, 4],	" ;
tmpStr +="	                                          [0, 5, 5],	" ;
tmpStr +="	                                          [0, 6, 1],	" ;
tmpStr +="	                                          [1, 0, 3],	" ;
tmpStr +="	                                          [1, 1, 2],	" ;
tmpStr +="	                                          [1, 2, 4],	" ;
tmpStr +="	                                          [1, 3, 0],	" ;
tmpStr +="	                                          [1, 4, 4],	" ;
tmpStr +="	                                          [1, 5, 5],	" ;
tmpStr +="	                                          [1, 6, 1],	" ;
tmpStr +="	                                          [2, 0, 5],	" ;
tmpStr +="	                                          [2, 1, 2],	" ;
tmpStr +="	                                          [2, 2, 1],	" ;
tmpStr +="	                                          [2, 3, 0],	" ;
tmpStr +="	                                          [2, 4, 4],	" ;
tmpStr +="	                                          [2, 5, 5],	" ;
tmpStr +="	                                          [2, 6, 1],	" ;
tmpStr +="	                                          [3, 0, 4],	" ;
tmpStr +="	                                          [3, 1, 3],	" ;
tmpStr +="	                                          [3, 2, 3],	" ;
tmpStr +="	                                          [3, 3, 0],	" ;
tmpStr +="	                                          [3, 4, 4],	" ;
tmpStr +="	                                          [3, 5, 5],	" ;
tmpStr +="	                                          [3, 6, 1],	" ;
tmpStr +="	                                          [4, 0, 3],	" ;
tmpStr +="	                                          [4, 1, 5],	" ;
tmpStr +="	                                          [4, 2, 2],	" ;
tmpStr +="	                                          [4, 3, 0],	" ;
tmpStr +="	                                          [4, 4, 4],	" ;
tmpStr +="	                                          [4, 5, 5],	" ;
tmpStr +="	                                          [4, 6, 1],	" ;
tmpStr +="	                                          [5, 0, 4],	" ;
tmpStr +="	                                          [5, 1, 1],	" ;
tmpStr +="	                                          [5, 2, 3],	" ;
tmpStr +="	                                          [5, 3, 0],	" ;
tmpStr +="	                                          [5, 4, 4],	" ;
tmpStr +="	                                          [5, 5, 5],	" ;
tmpStr +="	                                          [5, 6, 1],	" ;
tmpStr +="	                                          [6, 0, 3],	" ;
tmpStr +="	                                          [6, 1, 4],	" ;
tmpStr +="	                                          [6, 2, 2],	" ;
tmpStr +="	                                          [6, 3, 0],	" ;
tmpStr +="	                                          [6, 4, 4],	" ;
tmpStr +="	                                          [6, 5, 5],	" ;
tmpStr +="	                                          [6, 6, 1],	" ;
tmpStr +="	                                          [7, 0, 5],	" ;
tmpStr +="	                                          [7, 1, 1],	" ;
tmpStr +="	                                          [7, 2, 2],	" ;
tmpStr +="	                                          [7, 3, 0],	" ;
tmpStr +="	                                          [7, 4, 4],	" ;
tmpStr +="	                                          [7, 5, 5],	" ;
tmpStr +="	                                          [7, 6, 1],	" ;
tmpStr +="	                                          [8, 0, 1],	" ;
tmpStr +="	                                          [8, 1, 2],	" ;
tmpStr +="	                                          [8, 2, 2],	" ;
tmpStr +="	                                          [8, 3, 0],	" ;
tmpStr +="	                                          [8, 4, 4],	" ;
tmpStr +="	                                          [8, 5, 5],	" ;
tmpStr +="	                                          [8, 6, 1],	" ;
tmpStr +="	                                          [9, 0, 3],	" ;
tmpStr +="	                                          [9, 1, 2],	" ;
tmpStr +="	                                          [9, 2, 1],	" ;
tmpStr +="	                                          [9, 3, 0],	" ;
tmpStr +="	                                          [9, 4, 4],	" ;
tmpStr +="	                                          [9, 5, 5],	" ;
tmpStr +="	                                          [9, 6, 1],	" ;
tmpStr +="	                                          [10, 0, 0],	" ;
tmpStr +="	                                          [10, 1, 1],	" ;
tmpStr +="	                                          [10, 2, 0],	" ;
tmpStr +="	                                          [10, 3, 0],	" ;
tmpStr +="	                                          [10, 4, 4],	" ;
tmpStr +="	                                          [10, 5, 5],	" ;
tmpStr +="	                                          [10, 6, 1],	" ;
tmpStr +="	                                          [11, 0, 3],	" ;
tmpStr +="	                                          [11, 1, 2],	" ;
tmpStr +="	                                          [11, 2, 4],	" ;
tmpStr +="	                                          [11, 3, 0],	" ;
tmpStr +="	                                          [11, 4, 4],	" ;
tmpStr +="	                                          [11, 5, 5],	" ;
tmpStr +="	                                          [11, 6, 1],	" ;
tmpStr +="	                                          [12, 0, 5],	" ;
tmpStr +="	                                          [12, 1, 2],	" ;
tmpStr +="	                                          [12, 2, 1],	" ;
tmpStr +="	                                          [12, 3, 0],	" ;
tmpStr +="	                                          [12, 4, 4],	" ;
tmpStr +="	                                          [12, 5, 5],	" ;
tmpStr +="	                                          [12, 6, 1],	" ;
tmpStr +="	                                          [13, 0, 4],	" ;
tmpStr +="	                                          [13, 1, 3],	" ;
tmpStr +="	                                          [13, 2, 3],	" ;
tmpStr +="	                                          [13, 3, 0],	" ;
tmpStr +="	                                          [13, 4, 4],	" ;
tmpStr +="	                                          [13, 5, 5],	" ;
tmpStr +="	                                          [13, 6, 1],	" ;
tmpStr +="	                                          [14, 0, 3],	" ;
tmpStr +="	                                          [14, 1, 5],	" ;
tmpStr +="	                                          [14, 2, 2],	" ;
tmpStr +="	                                          [14, 3, 0],	" ;
tmpStr +="	                                          [14, 4, 4],	" ;
tmpStr +="	                                          [14, 5, 5],	" ;
tmpStr +="	                                          [14, 6, 1],	" ;
tmpStr +="	                                          [15, 0, 4],	" ;
tmpStr +="	                                          [15, 1, 1],	" ;
tmpStr +="	                                          [15, 2, 3],	" ;
tmpStr +="	                                          [15, 3, 0],	" ;
tmpStr +="	                                          [15, 4, 4],	" ;
tmpStr +="	                                          [15, 5, 5],	" ;
tmpStr +="	                                          [15, 6, 1],	" ;
tmpStr +="	                                          [16, 0, 3],	" ;
tmpStr +="	                                          [16, 1, 4],	" ;
tmpStr +="	                                          [16, 2, 2],	" ;
tmpStr +="	                                          [16, 3, 0],	" ;
tmpStr +="	                                          [16, 4, 4],	" ;
tmpStr +="	                                          [16, 5, 5],	" ;
tmpStr +="	                                          [16, 6, 1],	" ;
tmpStr +="	                                          [17, 0, 5],	" ;
tmpStr +="	                                          [17, 1, 1],	" ;
tmpStr +="	                                          [17, 2, 2],	" ;
tmpStr +="	                                          [17, 3, 0],	" ;
tmpStr +="	                                          [17, 4, 4],	" ;
tmpStr +="	                                          [17, 5, 5],	" ;
tmpStr +="	                                          [17, 6, 1],	" ;
tmpStr +="	                                          [18, 0, 1],	" ;
tmpStr +="	                                          [18, 1, 2],	" ;
tmpStr +="	                                          [18, 2, 2],	" ;
tmpStr +="	                                          [18, 3, 0],	" ;
tmpStr +="	                                          [18, 4, 4],	" ;
tmpStr +="	                                          [18, 5, 5],	" ;
tmpStr +="	                                          [18, 6, 1],	" ;
tmpStr +="	                                          [19, 0, 3],	" ;
tmpStr +="	                                          [19, 1, 2],	" ;
tmpStr +="	                                          [19, 2, 1],	" ;
tmpStr +="	                                          [19, 3, 0],	" ;
tmpStr +="	                                          [19, 4, 4],	" ;
tmpStr +="	                                          [19, 5, 5],	" ;
tmpStr +="	                                          [19, 6, 1],	" ;
tmpStr +="	                                          [20, 0, 0],	" ;
tmpStr +="	                                          [20, 1, 1],	" ;
tmpStr +="	                                          [20, 2, 0],	" ;
tmpStr +="	                                          [20, 3, 0],	" ;
tmpStr +="	                                          [20, 4, 4],	" ;
tmpStr +="	                                          [20, 5, 5],	" ;
tmpStr +="	                                          [20, 6, 1],	" ;
tmpStr +="	                                          [21, 0, 3],	" ;
tmpStr +="	                                          [21, 1, 2],	" ;
tmpStr +="	                                          [21, 2, 4],	" ;
tmpStr +="	                                          [21, 3, 0],	" ;
tmpStr +="	                                          [21, 4, 4],	" ;
tmpStr +="	                                          [21, 5, 5],	" ;
tmpStr +="	                                          [21, 6, 1],	" ;
tmpStr +="	                                          [22, 0, 5],	" ;
tmpStr +="	                                          [22, 1, 2],	" ;
tmpStr +="	                                          [22, 2, 1],	" ;
tmpStr +="	                                          [22, 3, 0],	" ;
tmpStr +="	                                          [22, 4, 4],	" ;
tmpStr +="	                                          [22, 5, 5],	" ;
tmpStr +="	                                          [22, 6, 1],	" ;
tmpStr +="	                                          [23, 0, 4],	" ;
tmpStr +="	                                          [23, 1, 3],	" ;
tmpStr +="	                                          [23, 2, 3],	" ;
tmpStr +="	                                          [23, 3, 0],	" ;
tmpStr +="	                                          [23, 4, 4],	" ;
tmpStr +="	                                          [23, 5, 5],	" ;
tmpStr +="	                                          [23, 6, 1],	" ;
tmpStr +="	                                          [24, 0, 3],	" ;
tmpStr +="	                                          [24, 1, 5],	" ;
tmpStr +="	                                          [24, 2, 2],	" ;
tmpStr +="	                                          [24, 3, 0],	" ;
tmpStr +="	                                          [24, 4, 4],	" ;
tmpStr +="	                                          [24, 5, 5],	" ;
tmpStr +="	                                          [24, 6, 1],	" ;
tmpStr +="	                                          [25, 0, 4],	" ;
tmpStr +="	                                          [25, 1, 1],	" ;
tmpStr +="	                                          [25, 2, 3],	" ;
tmpStr +="	                                          [25, 3, 0],	" ;
tmpStr +="	                                          [25, 4, 4],	" ;
tmpStr +="	                                          [25, 5, 5],	" ;
tmpStr +="	                                          [25, 6, 1],	" ;
tmpStr +="	                                          [26, 0, 3],	" ;
tmpStr +="	                                          [26, 1, 4],	" ;
tmpStr +="	                                          [26, 2, 2],	" ;
tmpStr +="	                                          [26, 3, 0],	" ;
tmpStr +="	                                          [26, 4, 4],	" ;
tmpStr +="	                                          [26, 5, 5],	" ;
tmpStr +="	                                          [26, 6, 1]	" ;
tmpStr +="	                            ]	" ;
tmpStr +="	             },	" ;
tmpStr +="		" ;
tmpStr +="	             \"valves\": {	" ;
tmpStr +="	                            \"categories\": [\"úôåç -214 âãåìä\", \"úôåç -214 ÷èðä\", \"áæ\", \"àâñ 1\", \"àâñ 2\"],	" ;
tmpStr +="	                            \"data\": []	" ;
tmpStr +="	             }	" ;
tmpStr +="	}]	" ;

	  return(tmpStr) ; 
  }
}
