package ap;

import java.io.IOException;
import org.json.JSONObject;
import cpcBridge.CpcBridgeAbstract;

public class PaymentNotification extends CpcBridgeAbstract<String> 
{

	public PaymentNotification()  
	{
		super();	
	}

	private static final long serialVersionUID = 1L;

	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public String parseJSON(String body) 
	{
		return body;

	}

	
		

	@Override
	public String fetchDataAndBuildResponse(String requestId , String inputData) 
	{
		ediUtils.startMethod();			
		String wsResponse= null;
		try 
		{
			ediUtils.writeToLog("the body is"+inputData);
			
			String cpcIP = ediUtils.getParameter("CPCbridge","URL");
			String url = "http://" + cpcIP +":8080/CpcbBackend/webapi/bridge/PaymentNotification";
			//String url = "http://" + cpcIP +":8080/CPCBridge/EBS/VerifyMembership";
			ediUtils.writeToLog("url is "+url);
			wsResponse = ediUtils.callWS(url, inputData, true);
			ediUtils.writeToLog("wsResponse="+wsResponse);
		} 
		catch (IOException e) 
		{
			
			e.printStackTrace();
			wsResponse = buildErrorReponse(requestId); 
			
		}
		return wsResponse;
	}

	@Override
	public String buildErrorReponse(String requestId ) 
	{
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "PaymentNotificationResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "PaymentNotificationRequest");
		main.put("Request",request);
		ediUtils.writeToLog("response is"+main.toString());		
		return main.toString();
		
		
	}



}
