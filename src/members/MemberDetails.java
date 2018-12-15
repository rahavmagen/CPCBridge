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


public class MemberDetails  extends CpcBridgeAbstract<UMNsData> 
{

	public MemberDetails()  
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
	
	
	@Override
	public UMNsData parseJSON(String body) 
	{
		JSONObject obj = new JSONObject(body);
		JSONArray arr = obj.getJSONArray("Umns");
		
		UMNsData  umnsData= new UMNsData();
		for (int i = 0; i < arr.length(); i++)
		{
			UMNdata umnData = umnsData.new UMNdata();
			umnData.umn = arr.getJSONObject(i).getString("Umn");
			umnsData.umnsList.add(umnData);

		}

		return umnsData;
		
	}

	
	public String buildSqlQuery(UMNdata inputData)
	{
		String sqlQuery="SELECT hp.party_number umn, hp.person_first_name,  hp.person_last_name,    hpp.gender, to_char(hpp.date_of_birth ,'dd/MM/yyyy') date_of_birth "+
			            "FROM   hz_parties hp, hz_person_profiles hpp " +
			            "WHERE  hp.party_type = 'PERSON' " +
			            "AND    hp.party_id = hpp.party_id " +
			            "AND    hp.party_number = '"+inputData.umn+"'";
				
		ediUtils.writeToLog("sqlquery is:"+sqlQuery + "util is"+ediUtils.toString());
		return sqlQuery;
	}



	private JSONObject buildErrorPart() 
	{
		JSONObject umnObj = new JSONObject();
		umnObj.put("Umn","");
		umnObj.put("FirstName","");
		umnObj.put("LastName", "");
		umnObj.put("DateOfBirth","");
		umnObj.put("Sex", "");
		umnObj.put("Status", "Failure");
		
		return umnObj;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId , UMNsData inputDataArr ) 
	{
		ediUtils.startMethod();
		String sqlQuery = null;
		JSONObject main = new JSONObject();
		try 
		{
			
			
			JSONObject response = new JSONObject();
			response.put("Id", getNextReqId());
			response.put("Type", "MemberDetailsResponse");
			response.put("Status","Success");
			main.put("Response", response);		
			JSONObject request = new JSONObject();
			request.put("Id", requestId);
			request.put("Type", "MemberDetailsRequest");
			main.put("Request",request);
			ediUtils.writeToLog("response is"+main.toString());
			JSONArray umns = new JSONArray();
			for(UMNdata inputdataRec: inputDataArr.umnsList)
			{
				ediUtils.writeToLog("the input data is "+inputdataRec);
				sqlQuery = buildSqlQuery(inputdataRec);
				ediUtils.writeToLog("sqlQuery is:"+sqlQuery + "util is"+ediUtils.toString());
				ResultSet resultSet = oraAppUtils.executeSqlQuery(sqlQuery);
				JSONObject umn = new JSONObject();
				if (resultSet.next() )
				{
					umn.put("Umn", checkNull(resultSet.getString("umn")));
					umn.put("FirstName", checkNull(resultSet.getString("person_first_name")));
					umn.put("LastName", checkNull(resultSet.getString("person_last_name")));
					umn.put("DateOfBirth", checkNull(resultSet.getString("date_of_birth")));
					umn.put("Sex", checkNull(resultSet.getString("gender")));
					umn.put("Status", "Success");
						
				}
				else
				{
					umn = buildErrorPart();
				}
				
				umns.put(umn);
					
			}
			main.put("Umns" , umns);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			String errResponse = buildErrorReponse(requestId);
			return errResponse;
		}
		
		
		ediUtils.writeToLog("response of MemberDetails is: "+main.toString());
		return main.toString();
	}

	@Override
	public String buildErrorReponse(String requestId ) 
	{
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "MemberDetailsResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "MemberDetailsRequest");
		main.put("Request",request);
		JSONObject umns = new JSONObject();
		main.put("Umns",umns);
				
		return main.toString();
		
		
	}


	



	
}
