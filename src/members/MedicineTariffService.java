package members;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import cpcBridge.CpcBridgeAbstract;
import members.Medicines.MedicineData;

public class MedicineTariffService  extends CpcBridgeAbstract<Medicines> 
{

	public MedicineTariffService() 
	{
		super();	
	}

	private static final long serialVersionUID = 11L;

	
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
	public Medicines parseJSON(String body) 
	{
		JSONObject obj = new JSONObject(body);
		JSONArray arr = obj.getJSONArray("Medicines");
		
		Medicines  medicinesData= new Medicines();
		for (int i = 0; i < arr.length(); i++)
		{
			MedicineData medicineData = medicinesData.new MedicineData();
			medicineData.MedicineCode = arr.getJSONObject(i).getString("MedicineCode");  
			medicineData.date = arr.getJSONObject(i).getString("Date");
		
			medicinesData.medicinesList.add(medicineData);

		}

		return medicinesData;
		
	}

	
	public String buildSqlQuery(MedicineData inputData)
	{
		String sqlQuery="SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM ,TO_CHAR(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') START_DATE_ACTIVE , TO_CHAR(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE "+ 
				"FROM   QP_LIST_LINES QPLL , "+ 
				"QP_PRICING_ATTRIBUTES QPPR ,"+
				"QP_LIST_HEADERS_TL QPLH , "+
				"MTL_SYSTEM_ITEMS_B MTL "+
				"WHERE  QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID "+ 
				"AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID  "+
				"AND QPLH.NAME='NHIS General Drug Tariffs'  "+
				"AND  QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "+ 
				"AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID  "+
				"AND MTL.SEGMENT1 = '"+inputData.MedicineCode+"' "+
				"AND TO_DATE('"+inputData.date +"','dd/MM/yyyy') BETWEEN TO_DATE(QPLL.START_DATE_ACTIVE ,  'dd/MM/yyyy') "
				+ "AND NVL(QPLL.END_DATE_ACTIVE , TO_DATE('"+inputData.date+"','dd/MM/yyyy' ) ) "; 
				
		return sqlQuery;
	}



	private JSONObject buildErrorPart(MedicineData inputdataRec) 
	{
		JSONObject medicineObj = new JSONObject();
		medicineObj.put("TariffVersionNo", "");
		medicineObj.put("MedicineCode",inputdataRec.MedicineCode);
		medicineObj.put("TariffValue", "");
		medicineObj.put("TariffUnit","");
		medicineObj.put("EffectiveFrom", "");
		medicineObj.put("EffectiveTo", "");
		medicineObj.put("Status", "Failure");
		
		return medicineObj;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId , Medicines inputDataArr ) 
	{
		ediUtils.startMethod();
		String sqlQuery = null;
		JSONObject main = new JSONObject();
		try 
		{
			
			
			JSONObject response = new JSONObject();
			response.put("Id", getNextReqId());
			response.put("Type", "MedicineTariffServiceResponse");
			response.put("Status","Success");
			main.put("Response", response);		
			JSONObject request = new JSONObject();
			request.put("Id", requestId);
			request.put("Type", "MedicineTariffServiceRequest");
			main.put("Request",request);
			ediUtils.writeToLog("response is"+main.toString());
			JSONArray medicines = new JSONArray();
			for(MedicineData inputdataRec: inputDataArr.medicinesList)
			{
				ediUtils.writeToLog("the input data is "+inputdataRec);
				sqlQuery = buildSqlQuery(inputdataRec);
				ediUtils.writeToLog("sqlQuery is:"+sqlQuery);
				ResultSet resultSet = oraAppUtils.executeSqlQuery(sqlQuery);
				JSONObject medicine = new JSONObject();
				if (resultSet.next() )
				{
					
					medicine.put("TariffVersionNo", ""); 
					medicine.put("MedicineCode", inputdataRec.MedicineCode);  
					medicine.put("TariffValue", checkNull(resultSet.getString("TARIFF_VALUE"))); 
					medicine.put("TariffUnit", checkNull(resultSet.getString("TARIFF_UOM"))); 
					medicine.put("EffectiveFrom", checkNull(resultSet.getString("START_DATE_ACTIVE")));
					medicine.put("EffectiveTo", checkNull(resultSet.getString("END_DATE_ACTIVE")));
					medicine.put("Status", "Success");
						
				}
				else
				{
					medicine = buildErrorPart(inputdataRec);
				}
				
				medicines.put(medicine);
					
			}
			main.put("Medicines" , medicines);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			String errResponse = buildErrorReponse(requestId);
			return errResponse;
		}
		
		
		ediUtils.writeToLog("response of tariffTypeService is: "+main.toString());
		return main.toString();
	}


	public String buildErrorReponse(String requestId ) 
	{
		
		
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "MedicineTariffServiceResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "MedicineTariffServiceRequest");
		main.put("Request",request);
		JSONObject medicines = new JSONObject();
		main.put("Medicines",medicines);
		
		return main.toString();
		
		
	}


	




}
