package lists;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class GDRGTariffs extends GetList 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12L;

	public GDRGTariffs()  {
		super();
		
	}

	@Override
	public String getServiceName() {
		
		return "GDRGTariffs";
	}

	@Override
	public JSONArray buildList(ResultSet resultSet) {

		ediUtils.startMethod();
		JSONArray listArr = new JSONArray();
		
		try    
		{
			
			while (resultSet.next())
			{
				JSONObject item = new JSONObject();
				
				item.put("TariffType", checkNull(resultSet.getString("TARIFF_TYPE")));
				item.put("GDRGCode", checkNull(resultSet.getString("GDRG_CODE")));
				item.put("Tariff", checkNull(resultSet.getString("TARIFF")));
				item.put("EffectiveDate", checkNull(resultSet.getString("START_DATE_ACTIVE")));
				item.put("ExpirationDate", checkNull(resultSet.getString("END_DATE_ACTIVE")));
				item.put("TariffVersionNumber","");
				item.put("HPN",""); /** TODO add HPN for the quaries*/
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("TariffType", "");
			item.put("GDRGCode", "");
			item.put("Tariff", "");
			item.put("EffectiveDate", "");
			item.put("ExpirationDate", "");
			item.put("TariffVersionNumber","");
			item.put("HPN","");
			item.put("Status", "Failure");
			e.printStackTrace();
			listArr.put(item);
		}
		
		
		
		return listArr;	
		
	
	}

	@Override
	public String buildSqlQuery(InputListValues inputList ) 
	{
		ediUtils.startMethod();
		ediUtils.writeToLog("getBy = "+inputList.getBy);
		StringBuilder  sb = new StringBuilder(); 
		
		switch(inputList.getBy)
		{
		case "All":
			
			sb = new StringBuilder(); 
			
			sb.append(" SELECT   QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE ,QPLL.OPERAND TARIFF ,to_char(QPLL.end_date_active,'dd/MM/yyyy') end_date_active ,to_char(QPLL.start_date_active,'dd/MM/yyyy') START_DATE_ACTIVE ");
			sb.append("FROM FND_FLEX_VALUE_SETS FFVS, FND_FLEX_VALUES FFV , QP_LIST_LINES QPLL ,QP_LIST_HEADERS_TL QPH ,QP_PRICING_ATTRIBUTES QPPR ,  " );
			sb.append("(select DISTINCT INVENTORY_ITEM_ID , segment1 from MTL_SYSTEM_ITEMS_B where  segment1 like '00%' and item_type = 'NHIA G-DRG') MTL   " );
			sb.append("WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID "); 
			sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' " );
			sb.append("AND QPLL.List_Header_Id = FFV.Attribute2 ");  
			sb.append("AND QPH.List_Header_Id = FFV.Attribute2 ");
			sb.append("AND QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID "); 
			sb.append("AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') " );
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID " );
			
			
			  break;
			
		case "Modified":
			
			
			sb = new StringBuilder(); 
			
			sb.append(" SELECT   QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE ,QPLL.OPERAND TARIFF ,to_char(QPLL.end_date_active,'dd/MM/yyyy') end_date_active ,to_char(QPLL.start_date_active,'dd/MM/yyyy') START_DATE_ACTIVE ");
			sb.append("FROM FND_FLEX_VALUE_SETS FFVS, FND_FLEX_VALUES FFV , QP_LIST_LINES QPLL ,QP_LIST_HEADERS_TL QPH ,QP_PRICING_ATTRIBUTES QPPR ,  " );
			sb.append("(select DISTINCT INVENTORY_ITEM_ID , segment1 from MTL_SYSTEM_ITEMS_B where  segment1 like '00%' and item_type = 'NHIA G-DRG') MTL   " );
			sb.append("WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID "); 
			sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' " );
			sb.append("AND QPLL.List_Header_Id = FFV.Attribute2 ");  
			sb.append("AND QPH.List_Header_Id = FFV.Attribute2 ");
			sb.append("AND QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID "); 
			sb.append("AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') " );
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID " );
			sb.append("AND QPLL.last_update_date between to_date('"+inputList.modifiedFrom+"','dd/MM/yyyy') ");
			sb.append("AND to_date('"+inputList.modifiedTo+"','dd/MM/yyyy')  ");
			
			
			break;
			
		case "Current":
			
			sb.append(" SELECT   QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE ,QPLL.OPERAND TARIFF ,to_char(QPLL.end_date_active,'dd/MM/yyyy') end_date_active ,to_char(QPLL.start_date_active,'dd/MM/yyyy') START_DATE_ACTIVE ");
			sb.append("FROM FND_FLEX_VALUE_SETS FFVS, FND_FLEX_VALUES FFV , QP_LIST_LINES QPLL ,QP_LIST_HEADERS_TL QPH ,QP_PRICING_ATTRIBUTES QPPR ,  " );
			sb.append("(select DISTINCT INVENTORY_ITEM_ID , segment1 from MTL_SYSTEM_ITEMS_B where  segment1 like '00%' and item_type = 'NHIA G-DRG') MTL   " );
			sb.append("WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID "); 
			sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' " );
			sb.append("AND QPLL.List_Header_Id = FFV.Attribute2 ");  
			sb.append("AND QPH.List_Header_Id = FFV.Attribute2 ");
			sb.append("AND QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID "); 
			sb.append("AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') " );
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID " );
			sb.append("and sysdate  <= nvl(QPLL.end_date_active,sysdate)");
			sb.append("and sysdate >= QPLL.start_date_active");

			break;
				
		case "Ids":
			
			sb = new StringBuilder(); 
			
			sb.append(" select  TARIFF_TYPE,  GDRG_CODE,  TARIFF, Start_Date_Active  , end_Date_Active  from ");
			sb.append("(SELECT QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE, QPLL.OPERAND TARIFF,  to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') Start_Date_Active  , to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') end_Date_Active  "); 
			sb.append("FROM FND_FLEX_VALUE_SETS FFVS,FND_FLEX_VALUES FFV, QP_LIST_LINES QPLL,QP_LIST_HEADERS_TL QPH,QP_PRICING_ATTRIBUTES QPPR,");
			sb.append("(select DISTINCT INVENTORY_ITEM_ID, segment1 ");
			sb.append("from MTL_SYSTEM_ITEMS_B ");
			sb.append("where segment1 like '00%' ");
			sb.append("and item_type = 'NHIA G-DRG' ");
			sb.append("and segment1 in (");
			buildIDsList(inputList , sb);
			sb.append(")) MTL ");
			sb.append("WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID ");
			sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' ");
			sb.append("and QPLL.List_Header_Id = FFV.Attribute2 ");
			sb.append("and QPH.List_Header_Id = FFV.Attribute2 ");
			sb.append("and QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') ");
			sb.append("and QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID ");
			sb.append("and  QPLL.Start_Date_Active <=sysdate  ) a ");
			sb.append("where   nvl(to_date(a.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))  = "); 
			sb.append("(select max(nvl(to_date(b.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))) "); 
			sb.append("from (SELECT QPH.name TARIFF_TYPE, MTL.Segment1 GDRG_CODE, QPLL.OPERAND TARIFF, QPLL.Start_Date_Active  , QPLL.end_Date_Active "); 
			sb.append("FROM FND_FLEX_VALUE_SETS FFVS, ");
			sb.append("FND_FLEX_VALUES FFV, ");
			sb.append("QP_LIST_LINES QPLL, ");
			sb.append("QP_LIST_HEADERS_TL QPH, ");
			sb.append("QP_PRICING_ATTRIBUTES QPPR, ");
			sb.append("(select DISTINCT INVENTORY_ITEM_ID, segment1 ");
			sb.append("from MTL_SYSTEM_ITEMS_B ");
			sb.append("where segment1 like '00%' ");
			sb.append("and item_type = 'NHIA G-DRG' ");
			sb.append("and segment1 in (");
			buildIDsList(inputList , sb);
			sb.append(")) MTL ");
			sb.append("WHERE FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID ");
			sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' ");
			sb.append("and QPLL.List_Header_Id = FFV.Attribute2 ");
			sb.append("and QPH.List_Header_Id = FFV.Attribute2 ");
			sb.append("and QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_LINE_TYPE_CODE IN ('PLL', 'PBH') ");
			sb.append("and QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID ");
			sb.append("and  QPLL.Start_Date_Active <=sysdate ) b )  ");
			
  
  
			
			break;
			
		default: 

			ediUtils.writeToLog("***********'getBy' input field is "+inputList.getBy+" it should be All or Modified or Current or Ids************");
			return null;
		}
		
	
		
		
		return sb.toString();
	}

	private void buildIDsList(InputListValues inputList, StringBuilder sb) 
	{
		Boolean firstTime = true;
		for(String id : inputList.ids)
		{
			if(firstTime)
			{
				sb.append("'");
				sb.append(id);
				sb.append("'");
				firstTime = false;
			}
			else
			{
				sb.append(",");
				sb.append("'");
				sb.append(id);
				sb.append("'");
			}
		}
	}





}
