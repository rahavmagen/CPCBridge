package cpcBridge;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

import edi.nhia.com.EDIUtils;



public  abstract class CpcBridgeAbstract <T> extends Servlet<T>
{

	public  abstract <T> T  parseJSON(String jsonBody);
		

	public abstract String fetchDataAndBuildResponse(String requestId , T inputData ) ;
	
	
	public  abstract String buildErrorReponse(String requestId );
	

	private static final long serialVersionUID = 1L;
	public final static String SYSTEM_NAME = "CPCAdapter" ;
	public final static String USER_NAME = "rahav" ;
	public EDIUtils ediUtils;
	public EDIUtils oraAppUtils;
	String requestId = "0000000000";
	
		
	@SuppressWarnings("unchecked")
	public String mainFlow(HttpServletRequest request) throws SQLException, NamingException, IOException , JSONException
	{
		 
		this.ediUtils    = new EDIUtils(SYSTEM_NAME, USER_NAME  );
		this.oraAppUtils = new EDIUtils(SYSTEM_NAME, USER_NAME );

		
		String responseStr = null;
		T inputDataArr = null;
			
		this.ediUtils.initDBByName("ediDBPostgres");
		this.oraAppUtils.initDBByName("OraApplication");
		
		this.ediUtils.initDebugFlag();
		String requestsBody = null;
		requestsBody = getBody(request);
		this.ediUtils.writeToLog("the body is"+requestsBody);
		populateRequestId(requestsBody);
		this.requestId = getRequestId();
		inputDataArr = (T) parseJSON(requestsBody);
		responseStr = fetchDataAndBuildResponse(this.requestId , inputDataArr );
		this.ediUtils.closeConnection();
		this.oraAppUtils.closeConnection();
		
		return responseStr;
	}

	
	
	public String getRequestId() {
		return this.requestId;
	}


	public String getBody(HttpServletRequest request) throws IOException
	{
		//ediUtils.startMethod();
		String body = null;
		if ("POST".equalsIgnoreCase(request.getMethod())) 
		{
			body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		}
		return body;
		
	}
	
	public String formatDate (Date inputDate) throws Exception
	{
			
		if(inputDate != null)
		{
			DateFormat toFormat = new SimpleDateFormat("dd/MM/yyyy");
			return toFormat.format(inputDate);
		}
		else
		{
			return null;
		}
		
	}
	
	public  void populateRequestId(String requestsBody) throws JSONException
	{
		
		JSONObject obj = new JSONObject(requestsBody);
		this.requestId =  obj.getJSONObject("Request").getString("Id");
		
	}
	
	public String buildReturnStatus(String returnStatus)
	{
		return ",\"Status\":\""+returnStatus+"\"";
	}
	
	public String getNextReqId () 
	{
		String sqlQuery = "select nextval('req_id')";
		String reqId = null;
		try 
		{
			ResultSet resultSet =  this.ediUtils.executeSqlQuery(sqlQuery);
			
			if (resultSet.next() )
			{
				reqId = resultSet.getString("NEXTVAL");
			}
		} 
		catch (SQLException e) 
		{
			reqId = "9999999999";
		}

		return reqId;
		
		
	}
	
	
	public String checkNull(String value)
	{
		if(value==null)
		{
			return "";
		}
		else
		{
			return value;
		}
	}

	
	
}

