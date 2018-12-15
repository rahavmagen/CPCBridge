package lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import cpcBridge.CpcBridgeAbstract;

public abstract class  GetList  extends CpcBridgeAbstract<InputListValues>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GetList()  {
		super();

	}

	
	@SuppressWarnings("unchecked")
	@Override
	public InputListValues parseJSON(String jsonBody) {
		JSONObject obj = new JSONObject(jsonBody);
		InputListValues inputListValues = new InputListValues();
		
		inputListValues.getBy  = obj.getJSONObject("RequestParameters").getString("By");
		switch (inputListValues.getBy)
		{
		case "Modified":
		
			inputListValues.modifiedFrom  = obj.getJSONObject("RequestParameters").getJSONObject("Modified").getString("From");
			inputListValues.modifiedTo = obj.getJSONObject("RequestParameters").getJSONObject("Modified").getString("To");	
			break;
		
			
		case "Ids":
			
			JSONArray arr = obj.getJSONObject("RequestParameters").getJSONArray("Ids");
			inputListValues.ids = new ArrayList<>();
			for (int i = 0; i < arr.length(); i++)
			{
				inputListValues.ids.add(arr.getJSONObject(i).getString("Id"));

			}
		}
		return inputListValues;
	}

	
	public abstract String buildSqlQuery(InputListValues inputData); 
	

	@Override
	public String fetchDataAndBuildResponse(String requestId, InputListValues inputData) {
		ediUtils.startMethod();
		String sqlQuery = buildSqlQuery(inputData );
		ediUtils.writeToLog("sqlQuery is:"+sqlQuery);
		
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		
		try 
		{
			response.put("Id", getNextReqId());	
			String serviceNameResponse = getServiceName() + "Response";
			response.put("Type", serviceNameResponse);
			response.put("Status","Success");
			main.put("Response", response);				
			JSONObject request = new JSONObject();
			request.put("Id", requestId);
			String serviceNameRequest = getServiceName() + "Request";
			request.put("Type", serviceNameRequest);
			main.put("Request", request);
			
			JSONObject list = new JSONObject();
			main.put("List",list );
			
		
			ResultSet resultSet = oraAppUtils.executeSqlQuery(sqlQuery ); 

			JSONArray items= buildList(resultSet ) ;
			main.put("List", items);
			
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			String errorResponse = buildErrorReponse(requestId);
			return errorResponse;
			
		}
			
			
		ediUtils.writeToLog("response of getList is: "+main.toString());
		return main.toString();
	}

	public abstract String getServiceName ();
	
	@Override
	public String buildErrorReponse(String requestId) 
	{
	 
		
		JSONObject main = new JSONObject();
		
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		String serviceNameResponse = getServiceName() + "Response";
		response.put("Type", serviceNameResponse);
		response.put("Status","Failure");
		main.put("Response", response);		
		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		String serviceNameRequest = getServiceName() + "Request";
		request.put("Type", serviceNameRequest);
		main.put("Request", request);
		
		JSONObject list = new JSONObject();
		main.put("List",list );
				
		return main.toString();
		
		
	}

	public abstract JSONArray buildList(ResultSet resultSet  );
	
	
	
}
