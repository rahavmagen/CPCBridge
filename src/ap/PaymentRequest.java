package ap;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import ap.PaymentReqInput.PaymentReq;
import cpcBridge.CpcBridgeAbstract;

public class PaymentRequest extends CpcBridgeAbstract<PaymentReqInput>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentRequest()  
	{
		super();
	
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public PaymentReqInput parseJSON(String jsonBody) 
	{
		
		ediUtils.startMethod();
		JSONArray arr = new JSONObject(jsonBody).getJSONArray("Batchs");
		
		PaymentReqInput paymentReqInput = new PaymentReqInput();
		paymentReqInput.requestId = new JSONObject(jsonBody).getJSONObject("Request").getString("Id");
		
		for (int i = 0; i < arr.length(); i++)
		{
			PaymentReq paymentReq = paymentReqInput.new PaymentReq();
			paymentReq.batchId = arr.getJSONObject(i).getString("Batch");
			paymentReq.healthFacilityCode = arr.getJSONObject(i).getString("HealthFacilityCode");
			paymentReq.tariffAmount = arr.getJSONObject(i).getString("TariffAmount");
			paymentReqInput.paymentsReq.add(paymentReq);
			
		}
		
		
		return paymentReqInput;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId, PaymentReqInput paymentReqInput) {

		ediUtils.startMethod();
		JSONObject main = new JSONObject();

		JSONObject response = new JSONObject();
		response.put("Id", getNextReqId());
		response.put("Type", "PaymentRequestResponse");
		response.put("Status","Success");
		main.put("Response", response);		

		JSONObject request = new JSONObject();
		request.put("Id", paymentReqInput.requestId);
		request.put("Type", "PaymentRequestRequest");
		main.put("Request", request);

		JSONArray  batchs =  buildBatchs( paymentReqInput);
		main.put("Batchs",batchs);

		ediUtils.writeToLog("response of getList is: "+main.toString());
		return main.toString();
		
	}

	@Override
	public String buildErrorReponse(String requestId)
	{
		
		JSONObject main = new JSONObject();
		
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "PaymentRequestResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "PaymentRequestRequest");
		main.put("Request", request);
		
		JSONObject Batchs = new JSONObject();
		main.put("Batchs",Batchs );
				
		return main.toString();
		
		
	}

	
	public JSONArray buildBatchs(PaymentReqInput paymentReqInput)
	{
		ediUtils.startMethod();	
		
		JSONArray jsonArray = new JSONArray();
		CallableStatement stmt = null;	

		String invoiceId;
		for(PaymentReq paymentRequest : paymentReqInput.paymentsReq)
		{	
			
			String spCallRequest= "BEGIN ? := apps.xx01_cpc_ap_pkg.xx01_create_invoice_f(?, ?, ? ) ;END;";					
			try 
			{
				stmt = oraAppUtils.connection.prepareCall(spCallRequest);
				stmt.setString(2,paymentRequest.healthFacilityCode);
				System.out.println("batch id is "+paymentRequest.batchId);
				stmt.setString(3,paymentRequest.batchId);
				stmt.setDouble(4,Double.parseDouble(paymentRequest.tariffAmount));
				stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
				stmt.executeUpdate();

				invoiceId =  stmt.getString(1);
				System.out.println ("invoice id ="+invoiceId);
				JSONObject batchDetails = new JSONObject();
				batchDetails.put("Batch", paymentRequest.batchId);
				batchDetails.put("HealthFacilityCode", paymentRequest.healthFacilityCode);
				batchDetails.put("TariffAmount", paymentRequest.tariffAmount);
				batchDetails.put("Invoice",String.valueOf(invoiceId));  
				
//				if ( invoiceId == 0)  // the SP return 0 if the invoice was not created 
				if("0".equals(invoiceId) || invoiceId == null)
				{
					this.ediUtils.writeToLog("***** invoice was not created ***");
					batchDetails.put("Status","Failure");
				}
				else
				{
					batchDetails.put("Status","Success");
				}
				jsonArray.put(batchDetails);
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
				JSONObject batchDetails = new JSONObject();
				batchDetails.put("Batch", paymentRequest.batchId);
				batchDetails.put("HealthFacilityCode", paymentRequest.healthFacilityCode);
				batchDetails.put("TariffAmount", paymentRequest.tariffAmount);
				batchDetails.put("Invoice","0");  
				batchDetails.put("Status","Failure");
				jsonArray.put(batchDetails);
			}
			
		}
		
		ediUtils.writeToLog("batch details are:"+jsonArray.toString());
		return jsonArray;
	}
			
}
