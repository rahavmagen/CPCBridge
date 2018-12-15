package ap;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import cpcBridge.CpcBridgeAbstract;

public class PaymentOrder  extends CpcBridgeAbstract<PaymentOrderInput>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentOrder()  
	{
		super();
	
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public PaymentOrderInput parseJSON(String jsonBody) 
	{
		
		this.ediUtils.startMethod();
		PaymentOrderInput paymentOrderInput = new PaymentOrderInput();
		paymentOrderInput.getBy = new JSONObject(jsonBody).getJSONObject("RequestParameters").getString("By");
		paymentOrderInput.requestId = new JSONObject(jsonBody).getJSONObject("Request").getString("Id");
		
		switch (paymentOrderInput.getBy)
		{
		case "Modified":
			
			
			JSONObject dates = new JSONObject(jsonBody).getJSONObject("RequestParameters").getJSONObject("Modified");
			
			InputDates inputDates = new InputDates();
			inputDates.fromDate = dates.getString("From");
			inputDates.ToDate = dates.getString("To");
			paymentOrderInput.getByValues.add(inputDates );
 
			break;
			
		case "Batches":
			
			JSONArray batchesArr = new JSONObject(jsonBody).getJSONObject("RequestParameters").getJSONArray("Batches");
			for(int i =0 ;i< batchesArr.length() ; i++)
			{
				paymentOrderInput.getByValues.add(batchesArr.getJSONObject(i).getString("Batch"));
			}
			
			break;
			
		case "Invoices":
			
			JSONArray InvoicesArr = new JSONObject(jsonBody).getJSONObject("RequestParameters").getJSONArray("Invoices");
			for(int i =0 ;i< InvoicesArr.length() ; i++)
			{
				paymentOrderInput.getByValues.add(InvoicesArr.getJSONObject(i).getString("Invoice"));
			}
			break;
			
		case "PaymentOrders":
			
			JSONArray PaymentOrdersArr = new JSONObject(jsonBody).getJSONObject("RequestParameters").getJSONArray("PaymentOrders");
			for(int i =0 ;i< PaymentOrdersArr.length() ; i++)
			{
				paymentOrderInput.getByValues.add(PaymentOrdersArr.getJSONObject(i).getString("PaymentOrder"));
			}
			
			break;
	
			
		}
		
		
		return paymentOrderInput;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId, PaymentOrderInput paymentOrderInput) 
	{

		this.ediUtils.startMethod();
		JSONObject main = new JSONObject();
	
		JSONObject response = new JSONObject();
		response.put("Id", getNextReqId());
		response.put("Type", "PaymentOrderResponse");
		response.put("Status","Success");
		main.put("Response", response);		

		JSONObject request = new JSONObject();
		request.put("Id", paymentOrderInput.requestId);
		request.put("Type", "PaymentOrderRequest");
		main.put("Request", request);
		
		JSONArray paymentOrders = new JSONArray();
		
		for(Object getByValue : paymentOrderInput.getByValues)
		{
			String sqlQuery = buildSqlQuery (paymentOrderInput , getByValue );
			try 
			{
				ResultSet resultSet = this.oraAppUtils.executeSqlQuery(sqlQuery );
				String previosPaymentOrder =" ";
				JSONArray invoices = new JSONArray();
				JSONObject paymentOrder = new JSONObject();
				JSONObject invoice =  new JSONObject();
				Boolean firstTime = true;
				Boolean paymentExists = false;
				while(resultSet.next() )
				{
					paymentExists = true;
					if(resultSet.getString("PaymentOrder").equals(previosPaymentOrder))
					{
						invoices.put(invoice);
						invoice =  new JSONObject(); 
						invoice.put("Amount", checkNull(resultSet.getString("amount")));
						invoice.put("Invoice", checkNull(resultSet.getString("invoice_num")));
						invoice.put("Batch", checkNull(resultSet.getString("batch_id")));
						invoice.put("PaymentFlag",checkNull( resultSet.getString("payment_flag")));
						invoice.put("PaymentReason",checkNull( resultSet.getString("payment_reason")));
						
						
						
					}
					else
					{
						if(!firstTime)
						{
							invoices.put(invoice);
							paymentOrder.put("Invoices",invoices);
							invoice =  new JSONObject(); 
							invoices = new JSONArray();
							paymentOrders.put(paymentOrder);
						}
						firstTime = false;
						paymentOrder = new JSONObject();
						paymentOrder.put("PaymentOrder", checkNull(resultSet.getString("PaymentOrder")));
						paymentOrder.put("HealthFacilityCode",checkNull( resultSet.getString("health_facility_code")));
						paymentOrder.put("CheckNumber",checkNull( resultSet.getString("check_number")));
						paymentOrder.put("PaymentDate",checkNull( resultSet.getString("payment_date")));
						paymentOrder.put("IsDeleted",checkNull( resultSet.getString("is_deleted")));
						paymentOrder.put("Remarks", checkNull(resultSet.getString("remarks")));
						
						
						invoice.put("Amount", checkNull(resultSet.getString("amount")));
						invoice.put("Invoice", checkNull(resultSet.getString("invoice_num")));
						invoice.put("Batch", checkNull(resultSet.getString("batch_id")));
						invoice.put("PaymentFlag",checkNull( resultSet.getString("payment_flag")));
						invoice.put("PaymentReason",checkNull( resultSet.getString("payment_reason")));
						
					
					}
					previosPaymentOrder = resultSet.getString("PaymentOrder");
				}
				if(paymentExists)
				{
					invoices.put(invoice);
					paymentOrder.put("Invoices",invoices);
					paymentOrders.put(paymentOrder);
				}
				
			}
				
			
			catch (SQLException e) 
			{
				buildErrorReponse(requestId);
				e.printStackTrace();
			}
		}

		main.put("PaymentOrders",paymentOrders);

		this.ediUtils.writeToLog("response of PaymentOrder is: "+main.toString());
		
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

	

	public String buildSqlQuery(PaymentOrderInput paymentOrderInput ,Object getByValue) 
	{
	
		this.ediUtils.startMethod();
		this.ediUtils.writeToLog("getBy = "+paymentOrderInput.getBy);
		StringBuilder  sqlQuery = new StringBuilder(); 
		sqlQuery.append("SELECT  ap.check_id PaymentOrder, "
						+"asa.segment1 health_facility_code, "
						+"ap.amount, "
						+"ai.invoice_num, "
						+"ai.attribute2 batch_id ,"
						+"aca.check_number ,"
						+"to_char(aca.check_date ,'dd/MM/yyyy') Payment_Date, "
						+"DECODE(ai.cancelled_date, '', 'N', 'Y') IS_DELETED, "
						+"aca.attribute1 remarks, "
						+"ap.attribute1 payment_flag, "
						+"ap.attribute2 payment_reason "
						+"FROM  ap_invoice_payments_all ap, "
						+"ap_invoices_all ai, " 
						+"ap_checks_all aca, "
						+"ap_suppliers asa "
						+"WHERE ap.invoice_id = ai.invoice_id "
						+"AND aca.check_id = ap.check_id "
						+"AND asa.vendor_id = ai.vendor_id "
						+"AND ai.attribute2 IS NOT NULL ");
		
		switch(paymentOrderInput.getBy)
		{
		case "Modified":
		
			sqlQuery.append("AND trunc(ap.creation_date) BETWEEN to_date('"+((InputDates)getByValue).fromDate+"', 'DD/MM/YYYY') "
						   +"AND   to_date('"+((InputDates)getByValue).ToDate+"','DD/MM/YYYY')");	
			
			break;
		case "Batches":
						
			this.ediUtils.writeToLog("batch list is"+(String)getByValue);
			
			sqlQuery.append(" AND   ai.attribute2 = '"+(String)getByValue+"'");
			
			break;
		case "Invoices":
			
			this.ediUtils.writeToLog("invoice list is"+(String)getByValue);
			
			sqlQuery.append(" AND ai.invoice_num = '"+(String)getByValue+"'");
			break;
		case "PaymentOrders":
			
			sqlQuery.append(" AND   ap.check_id ='"+(String)getByValue+"'");			
		}
		
		sqlQuery.append(" order by PaymentOrder");
		this.ediUtils.writeToLog("sql query is:"+sqlQuery.toString());
		
		return sqlQuery.toString();
	}

	
}
