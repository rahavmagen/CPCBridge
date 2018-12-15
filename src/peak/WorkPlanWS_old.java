package peak;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
public class WorkPlanWS_old extends HttpServlet {
/*********************************/
/* Back for the ngx heatmap      */ 
/*********************************/
	private static final int cTotalResultsCount = 24 ;
	private static final int cActualResultsCount = 3 ; /* Note: Now (+0) is part of the actual results section */
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WorkPlanWS_old() {
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
         * return a few data set
         * 
[{
                             "label": "pz1",
                             "data": [{
                                                          "name": "00:00",
                                                          "series": [{
                                                                                      "name": "pump1",
                                                                                      "value": 1
                                                                        },
                                                                        {
                                                                                      "name": "booster20",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "pump0_unit0",
                                                                                      "value": 2
                                                                        }
                                                          ]
                                           },

                                           {
                                                          "name": "09:00",
                                                          "series": [{
                                                                                      "name": "pump1",
                                                                                      "value": 1
                                                                        },
                                                                        {
                                                                                      "name": "pump1",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "pump1",
                                                                                      "value": 1
                                                                        }
                                                          ]
                                           }
                             ]
              },
              {
                             "label": "pz4",
                             "data": [{
                                                          "name": "00:00",
                                                          "series": [{
                                                                                      "name": "value1",
                                                                                      "value": 1
                                                                        },
                                                                        {
                                                                                      "name": "value2",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "value3",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "pump13",
                                                                                      "value": 2
                                                                        }
                                                          ]
                                           },

                                           {
                                                          "name": "09:00",
                                                          "series": [{
                                                                                      "name": "value1",
                                                                                      "value": 1
                                                                        },
                                                                        {
                                                                                      "name": "value2",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "value3",
                                                                                      "value": 0
                                                                        },
                                                                        {
                                                                                      "name": "pump13",
                                                                                      "value": 2
                                                                        }
                                                          ]
                                           }
                             ]
              }
]
 
		 *  
		 *  change log:
		 *  Date          Author     Reason
		 *  ===========   ========   ============================================
		 *  19-Nov-2017   Yoram      Initial Version
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
    try {
		conn = DriverManager.getConnection("jdbc:sqlserver://localhost;DatabaseName=pddb","sa","Advantech1");
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in connect URL\" }" );
		 return("{ result: \"Error in connect URL\" }" ) ;
	}
    
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
				  tmpResponseString += " [ ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"W",theTs)  ;
			      tmpResponseString += " ] ";
				tmpResponseString += " , ";
				tmpResponseString += " \"boosters\" : ";
				  tmpResponseString += " [ ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"B",theTs)  ;
			      tmpResponseString += " ] ";
				tmpResponseString += " , ";
				tmpResponseString += " \"valves\" : ";
				  tmpResponseString += " [ ";
				    tmpResponseString += getClassDataFromDB(result.getString("pz_id"),"V",theTs)  ;
				  tmpResponseString += " ] ";
				tmpResponseString += " } ";
			}
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
    	
		return (tmpResponseString) ;
	}

	private String getClassDataFromDB (String pPzId,String pClassification,Date pTs) {
		
		String tmpResults = "" ;
		Calendar c = Calendar.getInstance();
		
		
		for (int i = 1-cActualResultsCount ; i < cTotalResultsCount ; i++) {
			c.setTime(pTs) ;
			c.add(Calendar.HOUR,i);
			tmpResults += getClassDataForOneHourFromDB (pPzId,pClassification,c.getTime()) ;
			if (i < cTotalResultsCount - 1) {
				tmpResults += "," ;
			}
		} // end of loop over time stamps
		return tmpResults ;		
	}

	private String getClassDataForOneHourFromDB (String pPzId,String pClassification,Date pTs) {
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

	try {
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get driver class\" }" );
		return ("{ result: \"Error in get driver class\" }");
	}
    try {
		conn = DriverManager.getConnection("jdbc:sqlserver://localhost;DatabaseName=pddb","sa","Advantech1");
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in connect URL\" }" );
		 return("{ result: \"Error in connect URL\" }" ) ;
	}
    
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
    
    String tmpResults = "";
    QryString = null ;
    
    SimpleDateFormat df_name = new SimpleDateFormat("HH:mm") ;  
    SimpleDateFormat df_sql = new SimpleDateFormat("yyyy-MM-dd HH:mm") ;
    
    tmpResults = " { " ;
    
    tmpResults += " \"name\" : \"" + df_name.format(pTs) + "\"" ;
    tmpResults += " , " ;
    tmpResults += " \"series\" : " ;
    tmpResults += " [ " ;
    
    String innerTmpResults = "" ;  // We will build the elements here
	// Build the result prefix
	    
/*
 * The logic is
 * if the time stamp is in the past - read from the normelized table.
 * if the time stamp is in the future - check if there are constrains
 * if the time stamp is in the future and there are no constrains - take the planned value for the approved plan
 * 
 *  Note: the future query is one - using not exists and union
 * 
 * discrite return values are:
0 – closed (actual value)
1 – Open  (actual value)
2 – closed (planned)
3 – open (planned)
4 – closed (forces via constraints)
5 – open (via constrains)

 */
    Date now = new Date() ;
    
	if (pTs.before(now)) {
	    QryString = "select el.element_name_hebrew,CASE WHEN ev.Value = 0 THEN 0 ELSE 1 END as value " ;
	    QryString +=" from elements el inner join scada_meters m on el.element_id = m.element_id " ;
	    QryString +=" inner join Normalized_Events ev on m.meter_id = ev.meter_id " ;
	    QryString +=" where el.pz_id = " + pPzId ;
	    QryString +=" and el.classification = '" + pClassification +"'" ;
	    QryString +=" and m.meter_type in (select meter_type from meter_types where kind = 'S')" ;
	    QryString +=" and ev.time_value = '" + df_sql.format(pTs) + "'" ;
	    QryString +=" order by 1" ;
	}
	else {
	    QryString = "select el.element_name_hebrew,CASE WHEN ev.Value = 0 THEN 2 ELSE 3 END as value " ;
	    QryString +=" from elements el inner join scada_meters m on el.element_id = m.element_id " ;
	    QryString +=" inner join Planned_Events ev on m.meter_id = ev.meter_id " ;
	    QryString +=" where el.pz_id = " + pPzId ;
	    QryString +=" and el.classification = '" + pClassification +"'" ;
	    QryString +=" and m.meter_type in (select meter_type from meter_types where kind = 'S')" ;
	    QryString +=" and ev.time_value = '" + df_sql.format(pTs) + "'" ;
	    QryString +=" and ev.run_id  = (select parameter_value from parameters where parameter_name = 'APPROVED_PLAN')" ;
	    QryString +=" and not exists (select 'Found constraint' from Constraints c" ;
	    QryString +="   where c.meter_id = ev.meter_id and ev.time_value between c.from_timestamp and c.to_timestamp)" ;
	    QryString +=" union " ;
	    QryString += "select el.element_name_hebrew,CASE WHEN ev.Forced_Value = 0 THEN 4 ELSE 5 END as value " ;
	    QryString +=" from elements el inner join scada_meters m on el.element_id = m.element_id " ;
	    QryString +=" inner join Constraints ev on m.meter_id = ev.meter_id " ;
	    QryString +=" where el.pz_id = " + pPzId ;
	    QryString +=" and el.classification = '" + pClassification +"'" ;
	    QryString +=" and m.meter_type in (select meter_type from meter_types where kind = 'S')" ;
	    QryString +=" and '" + df_sql.format(pTs) + "'  between ev.from_timestamp and ev.to_timestamp" ;
	    QryString +=" order by 1" ;
		
	}
		try {
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in get elements and statuses reading sql\" }" );
		 return("{ result: \"Error in get elements and statuses reading sql: " + QryString + "\" }") ;
	}
    
    // get the data
     try {
		while (result.next()) {
			if (!innerTmpResults.equals("")) {
				innerTmpResults += "," ;
			}
			innerTmpResults += "{" ;
			innerTmpResults += "\"name\" : \"" + result.getString(1) + "\"" ;
			innerTmpResults += "," ;
			innerTmpResults += "\"value\" : \"" + result.getString(2) + "\"" ; ;
			innerTmpResults += "}" ;
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
	    tmpResults += innerTmpResults ;
	    tmpResults += " ] " ;
	    tmpResults += " } " ;
    	
		return (tmpResults) ;
	}
	
	String trimName(String s) {
		if (s.length() > 18) {
			return (s.substring(0, 15) + "...") ;
		}
		else {
			// right pad
			// return String.format("%1$-18s", s) ;
			// left pad
			return String.format("%1$18s", s) ;
		}
			
	}
	String mockupData () {
		return (" {\"label\" : \"אזור ניסוי 1\" ,  \"wells\" :  [  {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"2\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"2\"} ]  } , {  \"name\" : \"11:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"3\"},{\"name\" : \"חתול 2\",\"value\" : \"4\"} ]  } , {  \"name\" : \"12:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"5\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"13:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"2\"},{\"name\" : \"חתול 2\",\"value\" : \"3\"} ]  } , {  \"name\" : \"14:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"4\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"15:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"16:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"17:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"18:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"19:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"20:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"21:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"22:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"23:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"00:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"01:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"02:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"03:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"04:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"05:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"06:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"07:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"08:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"1\"} ]  } , {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"0\"},{\"name\" : \"חתול 1\",\"value\" : \"0\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"כלב 1\",\"value\" : \"1\"},{\"name\" : \"חתול 1\",\"value\" : \"1\"},{\"name\" : \"חתול 2\",\"value\" : \"0\"} ]  }  ]  ,  \"boosters\" :  [  {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"2\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"3\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"4\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"5\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"2\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"3\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"4\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"5\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"11:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"2\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"3\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"4\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"5\"} ]  } , {  \"name\" : \"12:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"13:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"14:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"15:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"16:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"17:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"18:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"19:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"20:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"21:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"22:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"23:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"00:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"01:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"02:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"03:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"04:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"05:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"0\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"06:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"1\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"07:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"1\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"1\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"08:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"1\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"1\"} ]  } , {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"1\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"גבינה יחידה 1\",\"value\" : \"1\"},{\"name\" : \"גבינה יחידה 2\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 1\",\"value\" : \"0\"},{\"name\" : \"תחנה יפית יחידה 2\",\"value\" : \"0\"},{\"name\" : \"נשר 2 לבקעות יחידה 1\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 2\",\"value\" : \"0\"},{\"name\" : \"עכבר יחידה 3\",\"value\" : \"0\"} ]  }  ]  ,  \"valves\" :  [  {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"11:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"12:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"13:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"14:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"15:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"1\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"16:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"17:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"18:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"19:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"20:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"21:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"22:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"23:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"00:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"01:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"02:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"03:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"04:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"05:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"06:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"0\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"07:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"08:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"09:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  } , {  \"name\" : \"10:00\" ,  \"series\" :  [ {\"name\" : \"תפוח -214 גדולה\",\"value\" : \"0\"},{\"name\" : \"תפוח -214 קטנה\",\"value\" : \"1\"},{\"name\" : \"בז\",\"value\" : \"0\"},{\"name\" : \"אגס 1\",\"value\" : \"0\"},{\"name\" : \"אגס 2\",\"value\" : \"0\"} ]  }  ]  }  ") ;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
