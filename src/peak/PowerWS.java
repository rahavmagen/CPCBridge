package peak;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class
 */
public class PowerWS extends HttpServlet {
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
	
	public HashMap<Integer,Integer> ratesVector = new HashMap<Integer, Integer>()  ;
	public HashMap<Integer,Float> costsVector = new HashMap<Integer, Float>()  ;
	
	public static float sumCostPeakActual = 0 ;  
	public static float sumCostShoulderActual = 0 ;  
	public static float sumCostOffPeakActual = 0 ;  

	public static float sumEnergyPeakActual = 0 ;  
	public static float sumEnergyShoulderActual = 0 ;  
	public static float sumEnergyOffPeakActual = 0 ;
	
	public static float sumCostPeakApproved = 0 ;  
	public static float sumCostShoulderApproved = 0 ;  
	public static float sumCostOffPeakApproved = 0 ;  

	public static float sumEnergyPeakApproved = 0 ;  
	public static float sumEnergyShoulderApproved = 0 ;  
	public static float sumEnergyOffPeakApproved = 0 ;  


	public static float sumCostPeakCandidate = 0 ;  
	public static float sumCostShoulderCandidate = 0 ;  
	public static float sumCostOffPeakCandidate = 0 ;  

	public static float sumEnergyPeakCandidate = 0 ;  
	public static float sumEnergyShoulderCandidate = 0 ;  
	public static float sumEnergyOffPeakCandidate = 0 ;  
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PowerWS() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 **/
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		/*
		 * Logic:
		 *  This method will return the data for the Power Chart.
		 *  It will get three parameters via the path: Type (A, P) (e.g all, pressure zone) and value for type (if not A) and time value (date and time)
         * Will return 24 points -  24 predicted
         * Will return the values for the approved plan and the last run plan
         *
         * return a few data sets
         * 
         * The label will indicate the content:
         * rate – the rate type for the hour indicated (1 – off peak, 2 – shoulder, 3 – peak
         * energyactual – sum of power meters per selection per hour  - actual data (historical)
         * energyapproved – sum of power meters per selection per hour  - for the approved run (future only)
         * energycandidate – some of power meters per selection per hour – for the latest run (future only)
         * Energybreakdown – usage break down (for the whole period) for off peak, shoulder and peak
         * totalcost : The seven value for the first line in the table [total, sum for off peak, sum for shoulder , sum for peak, percentage for off peak,  percentage for shoulders, percentage for peak] – for the cost
         *            we be returned as a JSON object {  label: string;  total : number;    offpeak: number;  shoulder: number;  peak: number;  offpeakpct: number;  shoulderpct: number;  peakpct: number;  }
         * totalenergy : The seven value for the second line in the table [total, sum for off peak, sum for shoulder , sum for peak, percentage for off peak,  percentage for shoulders, percentage for peak] – for the energy
         *            we be returned as a JSON object {  label: string;  total : number;    offpeak: number;  shoulder: number;  peak: number;  offpeakpct: number;  shoulderpct: number;  peakpct: number;  }
         *            (each pie / data table apear twice. once for approved plan and one for candidate plan)
         *
 
 {
	"datagraph": [{
		"label": "energyactual",
		"data": ["1402", "1397.2", "1116.9", "974.2", "858.5", "307.9", "667.7", "920.1", "1331.9", "1405.9", "1250", "1252.3", "1291.7", "1421.9", "1365.5", "1290.4", "1427.1", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423", "1423"]
	}, {
		"label": "energyapproved",
		"data": ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"]
	}, {
		"label": "energycandidate",
		"data": ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"]
	}, {
		"label": "rate",
		"data": ["1", "1", "1", "1", "1", "1", "1", "1", "1", "2", "2", "2", "3", "3", "3", "3", "3", "3", "3", "2", "2", "2", "1", "1", "1", "1", "1", "1", "1", "1"]
	}],
	"datapie": [{
		"label": "energyapproved",
		"data": ["53", "21", "26"]
	}, {
		"label": "energycandidate",
		"data": ["53", "21", "26"]
	}],
	"datatable": [{
			"label": "energyapproved",
			"data": [{
					"label": "Total Cost",
					"total": "1776445",
					"offpeak": "552988",
					"shoulder": "330358",
					"peak": "893097",
					"offpeakpct": "31",
					"shoulderpct": "18",
					"peakpct": "51"
				},
				{
					"label": "Total Energy",
					"total": "38180",
					"offpeak": "20360",
					"shoulder": "8177",
					"peak": "9642",
					"offpeakpct": "53",
					"shoulderpct": "21",
					"peakpct": "26"
				}
			]
		},
		{
			"label": "energycandidate",
			"data": [{
					"label": "Total Cost",
					"total": "1776445",
					"offpeak": "552988",
					"shoulder": "330358",
					"peak": "893097",
					"offpeakpct": "31",
					"shoulderpct": "18",
					"peakpct": "51"
				},
				{
					"label": "Total Energy",
					"total": "38180",
					"offpeak": "20360",
					"shoulder": "8177",
					"peak": "9642",
					"offpeakpct": "53",
					"shoulderpct": "21",
					"peakpct": "26"
				}
			]
		}
	]

}
 
		 *  
		 *  change log:
		 *  Date          Author     Reason
		 *  ===========   ========   ============================================
		 *  26-Sep-2017   Yoram      Initial Version
		 *  20-Oct-2017   Yoram      change JSON to meet highchart (and Sarah's) need (object and not array. structure etc.)
		 *  24-Oct-2017   Yoram      add actual energy data set
		 *  31-oct-2017   Yoram      Split pie and table to planned and candidate as well
		 *                           Also remove label constant and hard code them into the json
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
			sumCostPeakActual = 0 ;  
			sumCostShoulderActual = 0 ;  
			sumCostOffPeakActual = 0 ;  

			sumEnergyPeakActual = 0 ;  
			sumEnergyShoulderActual = 0 ;  
			sumEnergyOffPeakActual = 0 ;  

			sumCostPeakApproved = 0 ;  
			sumCostShoulderApproved = 0 ;  
			sumCostOffPeakApproved = 0 ;  

			sumEnergyPeakApproved = 0 ;  
			sumEnergyShoulderApproved = 0 ;  
			sumEnergyOffPeakApproved = 0 ;  

			sumCostPeakCandidate = 0 ;  
			sumCostShoulderCandidate = 0 ;  
			sumCostOffPeakCandidate = 0 ;  

			sumEnergyPeakCandidate = 0 ;  
			sumEnergyShoulderCandidate = 0 ;  
			sumEnergyOffPeakCandidate = 0 ;  
			
	        String RatesResults = "" ; 
	        /* Prepare the result string */
	        ResponseString="{ " ;
	        /* Get Data  */
	        /* We retreive the rate first as we need the cost vector, but we will add it later (for the GUI to have it's layers OK */
	        
	        RatesResults = getRateDataFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ; // Also fills rates and costs vector
	        ResponseString += " \"datagraph\": [ " ;
	        ResponseString += getActualEnergyDataFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ; //  Also fills Summaries vectors
	        ResponseString += " , " ;
	        ResponseString += getApprovedEnergyDataFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ; //  Also fills Summaries vectors
	        ResponseString += " , " ;
	        ResponseString += getCandidateEnergyDataFromDB(pQueryType,pQueryFilter,pBaseDate,pBaseHour) ;
	        ResponseString += " , " ;
	        ResponseString += RatesResults ;
	        ResponseString += " ] , " ;
	        
	        ResponseString += " \"datapie\" : " ;
	        ResponseString += " [ " ;
	          ResponseString += " { \"label\": \"energyactual\" , \"data\":" + getActualBreakdown() + " } " ; 
	          ResponseString += " , " ;
	          ResponseString += " { \"label\": \"energyapproved\" , \"data\":" + getApprovedBreakdown() + " } " ; 
	          ResponseString += " , " ;
	          ResponseString += " { \"label\": \"energycandidate\" , \"data\":" + getCandidateBreakdown() + " } " ; 
	        ResponseString += " ] " ;
	        ResponseString += " , " ;
	        ResponseString += " \"datatable\" : " ;
	        ResponseString += " [ " ;
	          ResponseString += " { \"label\" : \"energyactual\" , \"data\":" ;
	    	  ResponseString += " [ " ;
	            ResponseString += getTotalCostActual() ; 
	            ResponseString += " , " ;
	            ResponseString += getTotalEnergyActual() ; 
	          ResponseString += " ] " ;
	          ResponseString += " } " ;
	          ResponseString += " , " ;
	          ResponseString += " { \"label\" : \"energyapproved\" , \"data\":" ;
	    	  ResponseString += " [ " ;
	            ResponseString += getTotalCostApproved() ; 
	            ResponseString += " , " ;
	            ResponseString += getTotalEnergyApproved() ; 
	          ResponseString += " ] " ;
	          ResponseString += " } " ;
	          ResponseString += " , " ;
	          ResponseString += " { \"label\" : \"energycandidate\" , \"data\":" ;
	    	  ResponseString += " [ " ;
	            ResponseString += getTotalCostCandidate() ; 
	            ResponseString += " , " ;
	            ResponseString += getTotalEnergyCandidate() ; 
	          ResponseString += " ] " ;
	          ResponseString += " } " ;
	        ResponseString += " ] " ;


	         // Close the whole response
	         ResponseString += " }";
	         
			
				// Set response header 
		 		response.setContentType("application/json;charset=UTF-8");

		 		response.getWriter().println(ResponseString );

		}
	
	private String getRateDataFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
		/* Will be called three times with mode for now,yesterday, and weekly summary */
	String QryString;
    Connection conn = null;
    Statement stmt = null;
    ResultSet result = null;

    String tmpResponseString = ""  ;

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
    int offSet = 0 ;
	// Build the result prefix
    tmpResponseString=" {  \"label\" : \"rate\" , \"data\" : [" ;
    	for (offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    String theDate ;
	        	    theDate = "dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime))" ;
	        	    
	        	    QryString= "select rate_id,rate from costing" ;
    		        QryString += " where " ;
    	            QryString +=" contract_id = " + Integer.toString(theContractId) ;
    	            QryString +=" and substring(season_months,datepart(month,"+theDate + "),1) = 'Y'" ;
    	            QryString +=" and substring(Day_category_dow,datepart(dw,"+theDate + "),1) = 'Y'" ;
    	            QryString +=" and substring(Rate_Category_hours,case datepart(hour,"+theDate + ") when 0 then 24 else datepart(hour,"+theDate + ") end,1) = 'Y'" ;
    	                	 
		result = stmt.executeQuery(QryString);
	} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("{ result: \"Error in calculated demand sql\" }" );
		 return("{ result: \"Error in calculated demand sql: " + QryString + "\" }") ;
	}
    
    // get the data
     try {
		if (result.next()) {
			if (result.getString("rate_id") == null) {
				tmpResponseString += "\"null\"" ;
				ratesVector.put(offSet,0) ;
				costsVector.put(offSet,(float)0) ;
			}
			
			else {
				tmpResponseString += "\"" + result.getString("rate") + "\"";
				ratesVector.put(offSet,result.getInt("rate_id")) ;
				costsVector.put(offSet,result.getFloat("rate")) ;
			}
		}
		else /* No data found */ {
			tmpResponseString += "null" ;
		    ratesVector.put(offSet,0) ;
		    costsVector.put(offSet,(float)0) ;
		}
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
        if (offSet < cTotalResultsCount) {
        	tmpResponseString += ", " ;
        }
    	}
    	tmpResponseString += " ] } " ;
    	
    	
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");

		}
    	
		return (tmpResponseString) ;
	}


	private String getActualEnergyDataFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
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
    tmpResponseString=" {  \"label\" : \"energyactual\" , \"data\" : [" ;
    	for (int offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    QryString = "select format(sum(value),'0.#') as total_sum from Normalized_Events " ;
	        	    QryString += " where time_value = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)) "  ;
	        	    QryString += " and time_value <= sysdatetime()  "  ; // only historical values (in fact. There can not be future data in this table - but we leave the check just to be 100% sure 
	        	    QryString += " and meter_id in (select meter_id from Scada_Meters where meter_type in (select meter_type from Meter_Types where Kind = 'P')" ;
	            	if (pQueryType.equals("P"))
	            		QryString += " and element_id in (select element_id from elements where pz_id = " + pQueryFilter + ") " ;
	            	/* Note: If type is A (all) - do not restrict at all */
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
				float tmpValue = Float.parseFloat(result.getString("total_sum")) ;
				tmpResponseString += "\"" + result.getString("total_sum") +"\"" ;
				// Also - sum up for the rest of the outputs
				/** NOTE: It could have been all dynamic. But for now we hard code it to the three rate types **/
				switch (ratesVector.get(offSet)) { /* 1 - off peak, 2 - shoulders, 3 - peak */
				   case 0: /* This means that the value for the hours was not detected - so we skip */
					       break ;
				   case 1: sumEnergyOffPeakActual += tmpValue ;
				           sumCostOffPeakActual += tmpValue * costsVector.get(offSet);
				           break ;
				   case 2: sumEnergyShoulderActual += tmpValue ;
		                   sumCostShoulderActual += tmpValue * costsVector.get(offSet);
		                   break ;
				   case 3: sumEnergyPeakActual += tmpValue ;
		                   sumCostPeakActual += tmpValue * costsVector.get(offSet);
		           break ;
				   default : 
				          System.out.println("{ result: \"Error in detecting the rate for the hour\" }" );
				          return("{ result: \"Error in detecting the rate for the hour\" }") ;
				}

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
    	
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
    	tmpResponseString += " ] } " ;
			return (tmpResponseString) ;
	}

	private String getApprovedEnergyDataFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
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
    tmpResponseString=" {  \"label\" : \"energyapproved\" , \"data\" : [" ;
    	for (int offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    QryString = "select format(sum(value),'0.#') as total_sum from Planned_Events " ;
	        	    QryString += " where run_id = (select parameter_value from parameters where parameter_name = 'APPROVED_PLAN') "  ; // just the approved plan
	        	    QryString += " and time_value >= dateadd(mi,-59,sysdatetime()) "  ; // only future values
	        	    QryString += " and time_value = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)) "  ;
	        	    QryString += " and meter_id in (select meter_id from Scada_Meters where meter_type in (select meter_type from Meter_Types where Kind = 'P')" ;
	            	if (pQueryType.equals("P"))
	            		QryString += " and element_id in (select element_id from elements where pz_id = " + pQueryFilter + ") " ;
	            	/* Note: If type is A (all) - do not restrict at all */
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
				float tmpValue = Float.parseFloat(result.getString("total_sum")) ;
				tmpResponseString += "\"" + result.getString("total_sum") +"\"" ;
				// Also - sum up for the rest of the outputs
				/** NOTE: It could have been all dynamic. But for now we hard code it to the three rate types **/
				switch (ratesVector.get(offSet)) { /* 1 - off peak, 2 - shoulders, 3 - peak */
				   case 0: /* This means that the value for the hours was not detected - so we skip */
					       break ;
				   case 1: sumEnergyOffPeakApproved += tmpValue ;
				           sumCostOffPeakApproved += tmpValue * costsVector.get(offSet);
				           break ;
				   case 2: sumEnergyShoulderApproved += tmpValue ;
		                   sumCostShoulderApproved += tmpValue * costsVector.get(offSet);
		                   break ;
				   case 3: sumEnergyPeakApproved += tmpValue ;
		                   sumCostPeakApproved += tmpValue * costsVector.get(offSet);
		           break ;
				   default : 
				          System.out.println("{ result: \"Error in detecting the rate for the hour\" }" );
				          return("{ result: \"Error in detecting the rate for the hour\" }") ;
				}
			}
		}
		else /* No data found */
			tmpResponseString += "\"null\"" ;
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error "+e.getMessage()  +" in fetch for sql:\" " + QryString + "} ") ;
		}
        if (offSet < cTotalResultsCount) {
     	   tmpResponseString += ", " ;
        }
    	}
    	tmpResponseString += " ] } " ;
    	
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
			return (tmpResponseString) ;
	}
	
	private String getCandidateEnergyDataFromDB (String pQueryType,String pQueryFilter,String pBaseDate,String pBaseHour) {
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
    int offSet = 0 ;
	// Build the result prefix
    tmpResponseString=" {  \"label\" : \"energycandidate\" , \"data\" : [" ;
    	for (offSet = (1 - cActualResultsCount); offSet <= cTotalResultsCount ; offSet++) {
	        try {
	        	    QryString = "select format(sum(value),'0.#') as total_sum from Planned_Events " ;
	        	    QryString += " where run_id = (select parameter_value from parameters where parameter_name = 'CANDIDATE_PLAN') "  ; // Just the Candidate plan
	        	    QryString += " and time_value >= dateadd(mi,-59,sysdatetime()) "  ; // only future values
	        	    QryString += " and time_value = dateadd(hour,"+Integer.toString(offSet) +  ",cast('"+pBaseDate+" "+pBaseHour+"' as datetime)) "  ;
	        	    QryString += " and meter_id in (select meter_id from Scada_Meters where meter_type in (select meter_type from Meter_Types where Kind = 'P')" ;
	            	if (pQueryType.equals("P"))
	            		QryString += " and element_id in (select element_id from elements where pz_id = " + pQueryFilter + ") " ;
	            	/* Note: If type is A (all) - do not restrict at all */
	        	    QryString += ")" ; // Close the sub query for meter id's
		            result = stmt.executeQuery(QryString);
	        } catch (SQLException e) {
		          e.printStackTrace();
		          System.out.println("{ result: \"Error in planned power plan sql\" }" );
		          return("{ result: \"Error in planned power plan sql: " + QryString + "\" }") ;
	              }
    // get the data
     try {
		if (result.next()) {
			if (result.getString("total_sum") == null)
				tmpResponseString += "\"null\"" ; 
			else {
				// Add to json results
				float tmpValue = Float.parseFloat(result.getString("total_sum")) ;
				tmpResponseString += "\"" + result.getString("total_sum") +"\"" ;
				// Also - sum up for the rest of the outputs
				/** NOTE: It could have been all dynamic. But for now we hard code it to the three rate types **/
				switch (ratesVector.get(offSet)) { /* 1 - off peak, 2 - shoulders, 3 - peak */
				   case 0: /* This means that the value for the hours was not detected - so we skip */
					       break ;
				   case 1: sumEnergyOffPeakCandidate += tmpValue ;
				           sumCostOffPeakCandidate += tmpValue * costsVector.get(offSet);
				           break ;
				   case 2: sumEnergyShoulderCandidate += tmpValue ;
		                   sumCostShoulderCandidate += tmpValue * costsVector.get(offSet);
		                   break ;
				   case 3: sumEnergyPeakCandidate += tmpValue ;
		                   sumCostPeakCandidate += tmpValue * costsVector.get(offSet);
		           break ;
				   default : 
				          System.out.println("{ result: \"Error in detecting the rate for the hour\" }" );
				          return("{ result: \"Error in detecting the rate for the hour\" }") ;
				}
			}
		}
		else /* No data found */
			tmpResponseString += "\"null\"" ;
		}
		catch (Exception e) {
			return ("{ \"label\" : \"Error in fetch\" ") ;
		}
        if (offSet < cTotalResultsCount) {
     	   tmpResponseString += ", " ;
        }
    	}
    	tmpResponseString += " ] } " ;
    	
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("error closing connection");
		}
    	return (tmpResponseString) ;
	}

	private static String getActualBreakdown() {
		
		float grandTotal = sumEnergyOffPeakActual + sumEnergyShoulderActual + sumEnergyPeakActual ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumEnergyOffPeakActual * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumEnergyShoulderActual * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" [ "
		       + "\"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"" + Integer.toString(displayPeakPct) + "\""
		       + "] ") ;
	}
	private static String getApprovedBreakdown() {
		
		float grandTotal = sumEnergyOffPeakApproved + sumEnergyShoulderApproved + sumEnergyPeakApproved ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumEnergyOffPeakApproved * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumEnergyShoulderApproved * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" [ "
		       + "\"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"" + Integer.toString(displayPeakPct) + "\""
		       + "] ") ;
	}

	private static String getCandidateBreakdown() {
		
		float grandTotal = sumEnergyOffPeakCandidate + sumEnergyShoulderCandidate + sumEnergyPeakCandidate ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumEnergyOffPeakCandidate * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumEnergyShoulderCandidate * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" [ "
		       + "\"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"" + Integer.toString(displayPeakPct) + "\""
		       + "] ") ;
	}
	
	private static String getTotalCostActual() {
		float grandTotal = sumCostOffPeakActual + sumCostShoulderActual + sumCostPeakActual ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumCostOffPeakActual * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumCostShoulderActual * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Cost\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumCostOffPeakActual)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumCostShoulderActual)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumCostPeakActual)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}

	private static String getTotalCostApproved() {
		float grandTotal = sumCostOffPeakApproved + sumCostShoulderApproved + sumCostPeakApproved ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumCostOffPeakApproved * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumCostShoulderApproved * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Cost\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumCostOffPeakApproved)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumCostShoulderApproved)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumCostPeakApproved)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}

	
	private static String getTotalCostCandidate() {
		float grandTotal = sumCostOffPeakCandidate + sumCostShoulderCandidate + sumCostPeakCandidate ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		    displayOffPeakPct = (int)((sumCostOffPeakCandidate * 100) / grandTotal) ;
		    displayShoulderPct = (int)((sumCostShoulderCandidate * 100) / grandTotal) ;
		    displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Cost\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumCostOffPeakCandidate)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumCostShoulderCandidate)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumCostPeakCandidate)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}
	
	private static String getTotalEnergyActual() {
		float grandTotal = sumEnergyOffPeakActual + sumEnergyShoulderActual + sumEnergyPeakActual ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		   displayOffPeakPct = (int)((sumEnergyOffPeakActual * 100) / grandTotal) ;
		   displayShoulderPct = (int)((sumEnergyShoulderActual * 100) / grandTotal) ;
		   displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Energy\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumEnergyOffPeakActual)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumEnergyShoulderActual)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumEnergyPeakActual)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}

	private static String getTotalEnergyApproved() {
		float grandTotal = sumEnergyOffPeakApproved + sumEnergyShoulderApproved + sumEnergyPeakApproved ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		   displayOffPeakPct = (int)((sumEnergyOffPeakApproved * 100) / grandTotal) ;
		   displayShoulderPct = (int)((sumEnergyShoulderApproved * 100) / grandTotal) ;
		   displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Energy\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumEnergyOffPeakApproved)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumEnergyShoulderApproved)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumEnergyPeakApproved)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}

	private static String getTotalEnergyCandidate() {
		float grandTotal = sumEnergyOffPeakCandidate + sumEnergyShoulderCandidate + sumEnergyPeakCandidate ;
		int displayOffPeakPct = 0 ;
		int displayShoulderPct = 0 ;
		int displayPeakPct = 0 ;
		if (grandTotal != 0) {
		/* We will round the OffPeak and Shoulder - and have peak as the remaining to make sure we show 100% */ 
		   displayOffPeakPct = (int)((sumEnergyOffPeakCandidate * 100) / grandTotal) ;
		   displayShoulderPct = (int)((sumEnergyShoulderCandidate * 100) / grandTotal) ;
		   displayPeakPct = 100 - (displayOffPeakPct + displayShoulderPct) ;
		}

		return (" { "
		       + " \"label\" : \"Total Energy\", "
		       + "\"total\" : \"" + Integer.toString((int)grandTotal) + "\", "
		       + "\"offpeak\" : \"" + Integer.toString((int)sumEnergyOffPeakCandidate)  + "\", "
		       + "\"shoulder\" : \"" + Integer.toString((int)sumEnergyShoulderCandidate)  + "\", "
		       + "\"peak\" : \"" + Integer.toString((int)sumEnergyPeakCandidate)  + "\", "
		       + "\"offpeakpct\" : \"" + Integer.toString(displayOffPeakPct) + "\", "
		       + "\"shoulderpct\" : \"" + Integer.toString(displayShoulderPct) + "\", "
		       + "\"peakpct\" : \"" + Integer.toString(displayPeakPct) + "\""
		       + " } ") ;
	}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doGet(request, response);
		}
		
}
