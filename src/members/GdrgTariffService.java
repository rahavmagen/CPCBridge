package members;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import cpcBridge.CpcBridgeAbstract;
import members.GdrgsData.GdrgData;


public class GdrgTariffService  extends CpcBridgeAbstract<GdrgsData> 
{

	public GdrgTariffService() 
	{
		super();	
	}

	private static final long serialVersionUID = 10L;

	
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
	public GdrgsData parseJSON(String body) 
	{
		JSONObject obj = new JSONObject(body);
		JSONArray arr = obj.getJSONArray("GDRGs");
		
		GdrgsData  gdrgsData= new GdrgsData();
		for (int i = 0; i < arr.length(); i++)
		{
			GdrgData gdrgData = gdrgsData.new GdrgData();
			gdrgData.gdrgCode = arr.getJSONObject(i).getString("GDRGCode");   
			gdrgData.date = arr.getJSONObject(i).getString("Date");
			gdrgData.hpn = arr.getJSONObject(i).getString("HPN");
			gdrgsData.GdrgsList.add(gdrgData);

		}

		return gdrgsData;
		
	}

	
	public String buildSqlQuery(GdrgData inputData)
	{
		String sqlQuery="SELECT distinct QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE, QPLL.OPERAND TARIFF, to_char(QPLL.Start_Date_Active , 'dd/MM/yyyy') Start_Date_Active ,to_char( QPLL.end_Date_Active ,'dd/MM/yyyy') end_Date_Active, "+
				inputData.hpn +" HPN " +
				"FROM FND_FLEX_VALUE_SETS FFVS, "+
				"FND_FLEX_VALUES FFV, "+ 
				"QP_LIST_LINES QPLL, "+
				"QP_LIST_HEADERS_TL QPH, "+
				"QP_PRICING_ATTRIBUTES QPPR, "+
				"(select DISTINCT INVENTORY_ITEM_ID, segment1 "+
				"   from MTL_SYSTEM_ITEMS_B "+
				"   where segment1 like '00%' "+
				"   and item_type = 'NHIA G-DRG' "+
				"   and segment1  = '"+inputData.gdrgCode+ "' ) MTL "+
				"	WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID "+
				"   AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' "+
				"   and QPLL.List_Header_Id = FFV.Attribute2 "+
				"   and QPH.List_Header_Id = FFV.Attribute2 "+
				"   and QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID "+
				"   AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') "+
				"   and QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "+
				"   and to_date ('"+inputData.date+"','dd/MM/yyyy') between  QPLL.Start_Date_Active  and QPLL.end_Date_Active "+ 
				"   and QPH.List_Header_Id = "+
				"      (select FFV.attribute2 "+
				"          from FND_FLEX_VALUES     FFV, "+
				"            FND_FLEX_VALUE_SETS FFVS, "+
				"            AP_SUPPLIERS        AP "+
				"          where FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' "+
				"          and FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID "+
				"          and ap.attribute1 = FFV.Flex_Value "+
				"          and ap.segment1 = '"+ inputData.hpn+ "') ";

				
		ediUtils.writeToLog("sqlquery is:"+sqlQuery);
		return sqlQuery;
	}



	private JSONObject buildErrorPart(GdrgData inputData) 
	{
		JSONObject gdrgObj = new JSONObject();
		gdrgObj.put("TariffType","");
		gdrgObj.put("TariffVersionNo", "");
		gdrgObj.put("GdrgCode",inputData.gdrgCode);
		gdrgObj.put("HPN",inputData.hpn);
		gdrgObj.put("TariffValue", "");
		gdrgObj.put("EffectiveFrom", "");
		gdrgObj.put("EffectiveTo", "");
		gdrgObj.put("Status", "Failure");
		
		return gdrgObj;
	}

	@Override
	public String fetchDataAndBuildResponse(String requestId , GdrgsData inputDataArr ) 
	{
		ediUtils.startMethod();
		String sqlQuery = null;
		JSONObject main = new JSONObject();
		try 
		{
			
			
			JSONObject response = new JSONObject();
			response.put("Id", getNextReqId());
			response.put("Type", "GdrgTariffServiceResponse");
			response.put("Status","Success");
			main.put("Response", response);		
			JSONObject request = new JSONObject();
			request.put("Id", requestId);
			request.put("Type", "GdrgTariffServiceRequest");
			main.put("Request",request);
			ediUtils.writeToLog("response is"+main.toString());
			JSONArray gdrgs = new JSONArray();
			for(GdrgData inputdataRec: inputDataArr.GdrgsList)
			{
				sqlQuery = buildSqlQuery(inputdataRec);
				ediUtils.writeToLog("sqlQuery is:"+sqlQuery);
				ResultSet resultSet = oraAppUtils.executeSqlQuery(sqlQuery);
				JSONObject gdrg = new JSONObject();
				if (resultSet.next() )
				{
					
					gdrg.put("TariffType",checkNull(resultSet.getString( "TARIFF_TYPE")));
					gdrg.put("TariffValue",checkNull(resultSet.getString( "TARIFF"))); 
					gdrg.put("TariffVersionNo", "");
					gdrg.put("GdrgCode", checkNull(resultSet.getString("GDRG_CODE")));
					gdrg.put("HPN",checkNull(resultSet.getString( "HPN")));
					gdrg.put("EffectiveFrom", checkNull(resultSet.getString("Start_Date_Active")));
					gdrg.put("EffectiveTo", checkNull(resultSet.getString("end_Date_Active")));
					gdrg.put("Status", "Success");
						
				}
				else
				{
					gdrg = buildErrorPart(inputdataRec);
				}
				
				gdrgs.put(gdrg);
					
			}
			main.put("Gdrgs" , gdrgs);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			String errResponse = buildErrorReponse(requestId);
			return errResponse;
		}
		
		
		ediUtils.writeToLog("response of GdrgTariff Service is: "+main.toString());
		return main.toString();
	}


	public String buildErrorReponse(String requestId ) 
	{
		
		
		JSONObject main = new JSONObject();
		JSONObject response = new JSONObject();
		response.put("Id", "9999999999");
		response.put("Type", "GdrgTariffServiceResponse");
		response.put("Status","Failure");
		main.put("Response", response);		
		JSONObject request = new JSONObject();
		request.put("Id", requestId);
		request.put("Type", "GdrgTariffServiceRequest");
		main.put("Request",request);
		JSONObject gdrgs = new JSONObject();
		main.put("Gdrgs",gdrgs);
		
		return main.toString();
		
		
	}

	
	

}
