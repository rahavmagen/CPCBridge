package members;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import cpcBridge.CpcBridgeAbstract;
import members.UMNsData.UMNdata;

public class VerifyMembership extends CpcBridgeAbstract<UMNsData> 
{

	public VerifyMembership()  
	{
		super();
		
		
	}

	private static final long serialVersionUID = 1L;

	
	public String getBody(HttpServletRequest request) throws IOException
	{
		ediUtils.startMethod();
		String body = null;
		if ("POST".equalsIgnoreCase(request.getMethod())) 
		{
			body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		}
		return body;
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	public UMNsData parseJSON(String body) 
	{
		ediUtils.startMethod();
		JSONObject obj = new JSONObject(body);
		JSONArray arr = obj.getJSONArray("Umns");

		UMNsData  umnsData= new UMNsData();
		for (int i = 0; i < arr.length(); i++)
		{
			UMNdata umnData = umnsData.new UMNdata();
			umnData.umn = arr.getJSONObject(i).getString("Umn");
			umnData.date = arr.getJSONObject(i).getString("Date");
			umnsData.umnsList.add(umnData);
		}

		return umnsData;
		
	}




	
	
	public String buildSqlQuery(UMNdata inputData)
	{
		ediUtils.startMethod();
		String sqlQuery= "select count (*) count from hz_parties p,hz_relationships r "
				+ "where p.party_id = r.object_id "
				+ "and p.party_number = '"+inputData.umn+"' "
				+ "and p.party_type = 'PERSON' "
				+ "	and to_date('"+inputData.date+"','dd/MM/yyyy') between r.start_date and nvl(r.end_date,add_months(r.start_date,12))";

		ediUtils.writeToLog("sqlquery is:"+sqlQuery);
		return sqlQuery;
	}



	
	public JSONObject buildUMNsPart(ResultSet resultSet  , UMNdata inputUmnData)  
	{
		ediUtils.startMethod();
		JSONObject umn = new JSONObject();
		umn.put("Umn", inputUmnData.umn);
		umn.put("Date", inputUmnData.date);
		try 
		{
			resultSet.next(); 
			if(resultSet.getInt("COUNT")==0)
			{
				umn.put("Valid", "No");
				ediUtils.writeToLog("umn "+inputUmnData.umn+" is not valid");
			}
			else
			{
				umn.put("Valid", "Yes");
				ediUtils.writeToLog("umn "+inputUmnData.umn+" is valid");
			}
			umn.put("Status", "Success");

		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			umn.put("Valid", "No");
			umn.put("Status", "Failure");

		}

		return umn;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId , UMNsData inputDataArr  ) 
	{
		ediUtils.startMethod();
		String sqlQuery = null;
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		
		try
		{
			
			response.put("Id", getNextReqId());
			response.put("Type", "VerifyMembershipResponse");
			response.put("Status","Success");
			main.put("Response", response);		
			JSONObject request = new JSONObject();
			request.put("Id", requestId);
			request.put("Type", "VerifyMembershipRequest");
			main.put("Request",request);
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String errResonse = buildErrorReponse(requestId);
			return errResonse;
		}	
		JSONArray umns = new JSONArray();
		for(UMNdata inputdataRec: inputDataArr.umnsList)
		{
			ediUtils.writeToLog("the input data is "+inputdataRec);
			sqlQuery = buildSqlQuery(inputdataRec);
			ediUtils.writeToLog("sqlQuery is:"+sqlQuery);
			ResultSet resultSet = null;
			try
			{
				resultSet = oraAppUtils.executeSqlQuery(sqlQuery);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				JSONObject umn = buildErrorPerReq(inputdataRec );
				umns.put(umn);
				
				continue;
			}
			JSONObject umn= buildUMNsPart(resultSet , inputdataRec) ;
			umns.put(umn);
		}
		main.put("Umns" , umns);
		

		
		return main.toString();
	}

	public String buildErrorReponse(String requestId ) 
	{	
		JSONObject main = new JSONObject();
		
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "VerifyMembershipResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "VerifyMembershipRequest");
		main.put("Request",request);
			
		return main.toString();
	}

	public JSONObject  buildErrorPerReq(UMNdata inputUmnData )
	{
		ediUtils.startMethod();
		JSONObject umn = new JSONObject();
		umn.put("Umn", inputUmnData.umn);
		umn.put("Date", inputUmnData.date);
		umn.put("Status", "Failure");
		
		
		return umn;
	}


	

}


