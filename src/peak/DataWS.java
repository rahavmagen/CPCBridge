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
public class DataWS extends HttpServlet {
	/*
	 *  
	 */
	private static final long serialVersionUID = 1L;
	private static final int c_mode_now = 1 ;
	private static final int c_mode_yest = 2 ;
	private static final int c_mode_weekly = 3 ;
	
	private static final int cTotalResultsCount = 27 ;
	private static final int cActualResultsCount = 3 ; /* Note: Now (+0) is part of the actual results section */
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataWS() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		/*
		 * Logic:
		 *  This method will return the data for the Demand actual and predicted Chart.
		 *  It will get three parameters via the path: DemandId, PredictRun Date and Predict run hour
		 *  It will return fix lists (three pairs, each with two lists) of values to show
		 *  The pairs are:
		 *  I. today
		 *  II. Yesterday
		 *  III. sum of last week
		 *  * For each pair there are two lists
		 *  1. actual data: -2 hours, -1 hours and now (taken from pd_demand_predictor_hist_t). and the rest is padded with zeros
		 *  2. Predicted data : three zeros to match the actuial ist and then +1 hour, +2 hours ...  (taken from pd_demand_predictor_hist_t)
		 *  in the prediction - we take only "last run"=Y
		 *  if values are missing - we pad with 0. 
		 *  
		 *  sample return:
		 *  [
		 *   { data: [X,Y,Z, 0,0,0,0,0,0, .....], label: "actual" } ,
		 *   { data: [0,0,0,A,B,C,D,...], label: "predict" }
		 *  ]
		 *  
		 *  change log:
		 *  Date          Author     Reason
		 *  ===========   ========   ============================================
		 *  23-Apr-2017   Yoram      Initial Version
		 *  28-Jul-2017   Yoram      If return value is null - return 0 (For Sarah's chart)
		 *  28-Jul-2017   Yoram      add yesterday and last week data sets
		 *  25-Sep-2017   Yoram      change null value to be null again (in the highchart - the null will be skipped, as needed  
		 */

			String PathParams=request.getPathInfo() ;
			
			String pQueryType=null ; /* D - Demand Node, P - Presure Zone */ 
			String pQueryFilter=null ;
			String pPredictDate=null ; /* Format yyyy-mm-dd */
			String pPredictHour=null ; /* format hh:mi */
			
			String ResponseString="" ;
			
			final String cActualLabel="actual" ;
			final String cPredictLabel="predict" ;

			final String cActualLabelYest="yesterday" ;

			final String cActualLabelWeekly="last week" ;

			

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
					pPredictDate=null ;
				else {
					pPredictDate=pQueryFilter.substring(pQueryFilter.indexOf('/') + 1) ;
					pQueryFilter=pQueryFilter.substring(0, pQueryFilter.indexOf('/')) ;
					if (pPredictDate.indexOf('/')==-1)
						pPredictHour=null ;
					else {
						pPredictHour=pPredictDate.substring(pPredictDate.indexOf('/') + 1) ;
						pPredictDate=pPredictDate.substring(0, pPredictDate.indexOf('/')) ;
					}
				}
			}
			// Check parameters //
			if (pQueryType==null || pQueryFilter == null || pPredictDate==null || pPredictHour==null ) {
				System.out.println("Error in get path variables "+pQueryType + " " + pQueryFilter + " " +pPredictDate + " " + pPredictHour );
				 response.getWriter().println("{ result: \"Error in get path variables "+pQueryType + " " + pQueryFilter + " " +pPredictDate + " " + pPredictHour + "  \" }") ;
				return ;
				
			}
			} 			
			// Fix hour. We get only the hour and we need hh:mm format
			
			pPredictHour = pPredictHour + ":00" ;

	        
	        /* Prepare the result string */
	        ResponseString="[ " ;
	        /* Get Data for now */
	        ResponseString += getDataFromDB (c_mode_now,pQueryType,pQueryFilter,pPredictDate,pPredictHour,cActualLabel,cPredictLabel) ;

	        /* Get Data for yesterday */
	        ResponseString += " , " ;
	        ResponseString += getHistoricalDataFromDB (c_mode_yest,pQueryType,pQueryFilter,pPredictDate,pPredictHour,cActualLabelYest) ;
	        /* Get Data for weekly */
	        ResponseString += " , " ;
	        ResponseString += getHistoricalDataFromDB (c_mode_weekly,pQueryType,pQueryFilter,pPredictDate,pPredictHour,cActualLabelWeekly) ;


	         // Close the whole response
	         
	         ResponseString += " ]";
	         
			
				// Set response header 
		 		response.setContentType("application/json;charset=UTF-8");

			 response.getWriter().println(ResponseString );

		}

	private String getDataFromDB (int pMode,String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour,String pActualLabel,String pPredictLabel)
	{
		/* Will be called three times with mode for now,yesterday, and weekly summary */
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;
    
    int rowNum=0 ;


	final String cDataTag="data";
	final String cLabelTag="label";
	
	String tmpResponseString = "" ;
	
	String pointZeroVal = "" ;


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
    /*
     * Get the first values from actual
     */
    QryString = null ;
    int offSet = 0 ;
	// Build the result prefix
    tmpResponseString=" { \""+cDataTag + "\": [" ;
    	for (offSet = (1 - cActualResultsCount); offSet <= 0 ; offSet++) {
	        try {
	        	switch (pMode) {
	        	case c_mode_now:
	        	case c_mode_yest: QryString= "select format(sum(demand_value),'0.#') as demand_value from demands_calculated" ;
	        	                  break ;
	        	case c_mode_weekly: QryString= "select format((sum(demand_value)/7),'0.#') as demand_value from demands_calculated" ;
	        	                  break ;
	        	default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
		           return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
	        	}
    	if (pQueryType.equals("D"))
    	  QryString += " where dn_id="+pQueryFilter + " and ";
    	if (pQueryType.equals("P"))
    		QryString += " where dn_id in (select dn_id from demand_nodes where pz_id = "+pQueryFilter + ") and " ;
    	/* Note: If type is A (all) - do not restrict by deamnd node at all */
    	if (pQueryType.equals("A"))
    		QryString += " where " ;
    	/* Now the rest of the query criteria */
    	/* First the date, the only one affected by the mode */
    	switch (pMode) {
    	case c_mode_now: QryString +=" demand_timestamp = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
    	                 break ;
    	case c_mode_yest: QryString +=" demand_timestamp = dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
                          break ;
    	case c_mode_weekly: QryString +=" demand_timestamp in (" ;
    			            QryString += "dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
    			            QryString += ",dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-2,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-3,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-4,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-5,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-6,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ") " ;
                         break ;
    	default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
		           return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
    	} /* end of case over mode */ 
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in calculated demand sql\" }" );
		 return("{ result: \"Error in calculated demand sql: " + QryString + "\" }") ;
	}
    
    // get over the data
     try {
		if (result.next()) {
			if (result.getString("demand_value") == null) {
				tmpResponseString += "\"null\"" ; // Was: "0" ;
				if (offSet == 0) {
					pointZeroVal = "null" ;
				}
				
			}
			else if (result.getString("demand_value").equals("")) {
				    tmpResponseString += "\"null\"" ; // "0" ;
					if (offSet == 0) {
						pointZeroVal = "null" ;
					}
			}
			else {
				    tmpResponseString += "\"" + result.getString("demand_value") + "\"" ;
					if (offSet == 0) {
				       pointZeroVal = result.getString("demand_value") ;
					}
			}
			tmpResponseString += "," ;
		 }
		else { /* no data found (should not happen) */
			tmpResponseString += "\"null\"," ; // was: "0," ;
			if (offSet == 0) {
				pointZeroVal = "null" ;
			}
		}
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in getting result set from calculated demand sql\" }" );
		 return("{ result: \"Error in getting result set from calculated demand sql\" }") ;
	}
    	} // End of looping over offsets
        /*
         *  pad the list of values with dummy null's 
         */
         for (rowNum=cActualResultsCount ; rowNum < cTotalResultsCount  ; rowNum++) {
        	 tmpResponseString += "\"null\"" ; 
				if (rowNum < cTotalResultsCount - 1)
					tmpResponseString += "," ;
         }
         try {
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in close result set\" }" );
			 return("{ result: \"Error in close result set\" }") ;
		}
         
    // Close the set
         
         tmpResponseString += "] , \""+cLabelTag +"\": \"" + pActualLabel + "\" } , " ;


    /*
     * Get the next values from prediction
     */
    // create the data set

    tmpResponseString += " { \"" + cDataTag + "\": [ " ;
         
    // add the dummy first values
    
    for (int i = 0;i < cActualResultsCount-1 ;i++) {
       tmpResponseString += "\"null\"," ; // was: "0,0,0," ; (091-oct-2017) : just two results. We WILL show the actual for point zero
    }
    rowNum=cActualResultsCount;

    tmpResponseString += "\"" + pointZeroVal + "\"," ;

    // Now get the rest of the values from predict
    QryString = null ;     
    for (offSet = 1 ; offSet < (cTotalResultsCount - cActualResultsCount) + 1 ; offSet++) {
    try {
    	switch (pMode) {
    	   case c_mode_now: 
    	   case c_mode_yest: QryString= "select format(sum(demand_value),'0.#') as demand_value from demands_predicted" ;
    	                    break ;
    	   case c_mode_weekly: QryString= "select format((sum(demand_value)/7),'0.#') as demand_value from demands_predicted" ;
    	                    break ;
    	   default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
                     return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
    	}    	                    
    	if (pQueryType.equals("D"))
        	  QryString += " where dn_id="+pQueryFilter + " and ";
    	if (pQueryType.equals("P"))
    		QryString += " where dn_id in (select dn_id from demand_nodes where pz_id = "+pQueryFilter + ") and " ;
    	if (pQueryType.equals("A"))
    		QryString += " where " ;
    	/* Now the rest of the query criteria */
    	QryString += " last_run='Y'" ;
    	/* The date, the only one affected by the mode */
    	QryString += " and " ;
    	switch (pMode) {
    	case c_mode_now: QryString +=" demand_timestamp = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
    	                 break ;
    	case c_mode_yest: QryString +=" demand_timestamp = dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
                          break ;
    	case c_mode_weekly: QryString +=" demand_timestamp in (" ;
    			            QryString += "dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
    			            QryString += ",dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-2,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-3,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-4,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-5,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ",dateadd(day,-6,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
    			            QryString += ") " ;
                         break ;
    	default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
		           return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
    	}
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in predict sql\" }" );
		 return("{ result: \"Error in predict sql: "+ QryString + "\" }") ;
	}
	// Build the result prefix for the first set - the predict
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("demand_value") == null)
				tmpResponseString += "\"null\"" ; // was: "0" ;
			else if (result.getString("demand_value").equals(""))
				    tmpResponseString += "\"null\"" ; // "0" ;
			     else
			        tmpResponseString += "\"" + result.getString("demand_value") + "\"" ;
			if (offSet < (cTotalResultsCount - cActualResultsCount))
				tmpResponseString += "," ;
		 }
		else { /* No data found - should not happen */
			tmpResponseString += "\"null\"" ; // was: "0" ;
			if (offSet < (cTotalResultsCount - cActualResultsCount))
				tmpResponseString += "," ;
		}
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in getting result set from predicted demand sql\" }" );
		 return("{ result: \"Error in getting result set from predicted demand sql\" }") ;
	}
    } // End of loop over off sets
    
     try {
		result.close();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in close result set\" }" );
		 return("{ result: \"Error in close result set\" }") ;
	}
     try {
		stmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in close statement\" }" );
		 return("{ result: \"Error in close statement\" }") ;
	}
     
// Close the set
     
     tmpResponseString += "] , \""+ cLabelTag + "\": \"" + pPredictLabel + "\" } " ;


     try {
		result.close();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in close result set\" }" );
		return("{ result: \"Error in close result set\" }") ;
	}
     try {
		stmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in close statement\" }" );
		return("{ result: \"Error in close statement\" }") ;
	}
     try {
		conn.close();
	} catch (SQLException e) {
		e.printStackTrace();
		 System.out.println("{ result: \"Error in close connection\" }" );
		 return("{ result: \"Error in close connection\" }") ;
	}
	
     
     return (tmpResponseString) ;
	
}
		/**
		 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
		 */
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			// TODO Auto-generated method stub
			doGet(request, response);
		}

		
		private String getHistoricalDataFromDB (int pMode,String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour,String pActualLabel)
		{
			/* Will be called three times with mode for now,yesterday, and weekly summary */
		String QryString;
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet result = null;
	    
		final String cDataTag="data";
		final String cLabelTag="label";
		
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
	    /*
	     * Get the values from actual
	     */
	    QryString = null ;
	    int offSet = 0 ;
		// Build the result prefix
	    tmpResponseString=" { \""+cDataTag + "\": [" ;
	    	for (offSet= (1 - cActualResultsCount); offSet < (cTotalResultsCount - cActualResultsCount) + 1 ; offSet++) {
		        try {
		        	switch (pMode) {
		        	case c_mode_now:
		        	case c_mode_yest: QryString= "select format(sum(demand_value),'0.#') as demand_value from demands_calculated" ;
		        	                  break ;
		        	case c_mode_weekly: QryString= "select format((sum(demand_value)/7),'0.#') as demand_value from demands_calculated" ;
		        	                  break ;
		        	default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
			           return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
		        	}
	    	if (pQueryType.equals("D"))
	    	  QryString += " where dn_id="+pQueryFilter + " and ";
	    	if (pQueryType.equals("P"))
	    		QryString += " where dn_id in (select dn_id from demand_nodes where pz_id = "+pQueryFilter + ") and " ;
	    	/* Note: If type is A (all) - do not restrict by deamnd node at all */
	    	if (pQueryType.equals("A"))
	    		QryString += " where " ;
	    	/* Now the rest of the query criteria */
	    	/* First the date, the only one affected by the mode */
	    	switch (pMode) {
	    	case c_mode_now: QryString +=" demand_timestamp = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
	    	                 break ;
	    	case c_mode_yest: QryString +=" demand_timestamp = dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	                          break ;
	    	case c_mode_weekly: QryString +=" demand_timestamp in (" ;
	    			            QryString += "dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
	    			            QryString += ",dateadd(day,-1,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ",dateadd(day,-2,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ",dateadd(day,-3,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ",dateadd(day,-4,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ",dateadd(day,-5,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ",dateadd(day,-6,dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)))" ;
	    			            QryString += ") " ;
	                         break ;
	    	default : System.out.println("{ result: \"Error in get data mode (now,yest,weekly)\" }" );
			           return("{ result: \"Error in get data mode (now,yest,weekly)\" }") ;
	    	} /* end of case over mode */ 
			result = stmt.executeQuery(QryString);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in calculated demand sql\" }" );
			 return("{ result: \"Error in calculated demand sql: " + QryString + "\" }") ;
		}
	    
	    // get over the data
	     try {
			if (result.next()) {
				if (result.getString("demand_value") == null)
					tmpResponseString += "\"null\"" ; // was: "0" ;
				else if (result.getString("demand_value").equals(""))
					    tmpResponseString += "\"null\"" ; // was: "0" ;
				else
					    tmpResponseString += "\"" + result.getString("demand_value") + "\"" ;
				if (offSet < (cTotalResultsCount - cActualResultsCount))
				   tmpResponseString += "," ;
			 }
			else { /* no data found (should not happen) */
				if (offSet < (cTotalResultsCount - cActualResultsCount))
				   tmpResponseString += "\"null\"," ; //was: "0," ;
				else 
					tmpResponseString += "\"null\"" ; // was: "0" ;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in getting result set from calculated demand sql\" }" );
			 return("{ result: \"Error in getting result set from calculated demand sql\" }") ;
		}
	    	} // End of looping over offsets
	         try {
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("{ result: \"Error in close result set\" }" );
				 return("{ result: \"Error in close result set\" }") ;
			}
	         
	    // Close the set
	         
	         tmpResponseString += "] , \""+cLabelTag +"\": \"" + pActualLabel + "\" } " ;

	     try {
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in close result set\" }" );
			return("{ result: \"Error in close result set\" }") ;
		}
	     try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("{ result: \"Error in close statement\" }" );
			return("{ result: \"Error in close statement\" }") ;
		}
	     try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			 System.out.println("{ result: \"Error in close connection\" }" );
			 return("{ result: \"Error in close connection\" }") ;
		}
		
	     return (tmpResponseString) ;
		
	}
		
}
