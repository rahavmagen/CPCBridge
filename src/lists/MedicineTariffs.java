package lists;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class MedicineTariffs extends GetList 
{

	/**
	 * test
	 */
	private static final long serialVersionUID = 12L;

	public MedicineTariffs()  {
		super();
		
	}

	@Override
	public String getServiceName() {
		
		return "MedicineTariffs";
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
				item.put("TariffVersionNo", "");
				item.put("MedicineCode", checkNull(resultSet.getString("MEDICINE_CODE")));
				item.put("TariffValue", checkNull(resultSet.getString("TARIFF_VALUE")));
				item.put("TariffUOM", checkNull(resultSet.getString("TARIFF_UOM")));
				item.put("EffectiveDate", checkNull(resultSet.getString("START_DATE_ACTIVE")));
				item.put("ExpirationDate", checkNull(resultSet.getString("END_DATE_ACTIVE")));
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("TariffVersionNo", "");
			item.put("MedicineCode","");
			item.put("TariffValue", "");
			item.put("TariffUOM", "");
			item.put("EffectiveDate", "");
			item.put("ExpirationDate", "");
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
			sb.append(" select MEDICINE_CODE ,  TARIFF_VALUE ,TARIFF_UOM ,START_DATE_ACTIVE, END_DATE_ACTIVE  from");
			sb.append("( SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') " );
			sb.append("START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE " );
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL "); 
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID " );
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' ");
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append(") a ");
			sb.append("Where  nvl(to_date(a.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))  = ");     
			sb.append("(select max(nvl(to_date(b.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))) ");
			sb.append("from (SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE ");
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL ");
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' "); 
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append(" ) b ");
			sb.append("where  a.medicine_code =  b.MEDICINE_CODE )");
			sb.append("  ORDER BY  MEDICINE_CODE " );
			
			  break;
			
		case "Modified":
			
			
			sb = new StringBuilder(); 
			
			sb.append(" select MEDICINE_CODE ,  TARIFF_VALUE ,TARIFF_UOM ,START_DATE_ACTIVE, END_DATE_ACTIVE  from");
			sb.append("( SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') " );
			sb.append("START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE " );
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL "); 
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID " );
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' ");
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND MTL.last_update_date between to_date('"+inputList.modifiedFrom+"','dd/MM/yyyy') and to_date('"+inputList.modifiedTo +"','dd/MM/yyyy')  ");
			sb.append(") a ");
			sb.append("Where  nvl(to_date(a.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))  = ");     
			sb.append("(select max(nvl(to_date(b.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))) ");
			sb.append("from (SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE ");
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL ");
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' "); 
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND MTL.last_update_date between to_date('"+inputList.modifiedFrom+"','dd/MM/yyyy') and to_date('"+inputList.modifiedTo +"','dd/MM/yyyy')  ");
			sb.append(" ) b ");
			sb.append("where  a.medicine_code =  b.MEDICINE_CODE )");
			sb.append("  ORDER BY  MEDICINE_CODE " );
			
			
			break;
			
		case "Current":
			
			sb.append(" select MEDICINE_CODE ,  TARIFF_VALUE ,TARIFF_UOM ,START_DATE_ACTIVE, END_DATE_ACTIVE  from");
			sb.append("( SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') " );
			sb.append("START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE " );
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL "); 
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID " );
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' ");
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND nvl(QPLL.END_DATE_ACTIVE,to_date('31/12/4000', 'dd/MM/yyyy')) > sysdate and QPLL.START_DATE_ACTIVE <= sysdate  ");
			sb.append(") a ");
			sb.append("Where  nvl(to_date(a.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))  = ");     
			sb.append("(select max(nvl(to_date(b.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))) ");
			sb.append("from (SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE ");
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL ");
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' "); 
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND nvl(QPLL.END_DATE_ACTIVE,to_date('31/12/4000', 'dd/MM/yyyy')) > sysdate and QPLL.START_DATE_ACTIVE <= sysdate");
			sb.append(" ) b ");
			sb.append("where  a.medicine_code =  b.MEDICINE_CODE )");
			sb.append("  ORDER BY  MEDICINE_CODE " );

			break;
				
		case "Ids":
			
			sb = new StringBuilder(); 
			
			sb.append(" select MEDICINE_CODE ,  TARIFF_VALUE ,TARIFF_UOM ,START_DATE_ACTIVE, END_DATE_ACTIVE  from");
			sb.append("( SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') " );
			sb.append("START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE " );
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL "); 
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID " );
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' ");
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND  MTL.segment1 IN (");
			buildIDsList(inputList, sb);
			sb.append(")) a ");
			sb.append("Where  nvl(to_date(a.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))  = ");     
			sb.append("(select max(nvl(to_date(b.END_DATE_ACTIVE,'dd/MM/yyyy'), to_date('31/12/4000', 'dd/MM/yyyy'))) ");
			sb.append("from (SELECT DISTINCT MTL.SEGMENT1 MEDICINE_CODE , QPLL.operand TARIFF_VALUE ,QPPR.product_uom_code TARIFF_UOM , to_char(QPLL.START_DATE_ACTIVE,'dd/MM/yyyy') START_DATE_ACTIVE, to_char(QPLL.END_DATE_ACTIVE,'dd/MM/yyyy') END_DATE_ACTIVE ");
			sb.append("FROM   QP_LIST_LINES QPLL , QP_PRICING_ATTRIBUTES QPPR , QP_LIST_HEADERS_TL QPLH , MTL_SYSTEM_ITEMS_B MTL ");
			sb.append("WHERE QPPR.LIST_LINE_ID = QPLL.LIST_LINE_ID ");
			sb.append("AND QPLL.LIST_HEADER_ID=QPLH.LIST_HEADER_ID ");  
			sb.append("AND QPLH.NAME='NHIS General Drug Tariffs' "); 
			sb.append("AND QPPR.PRODUCT_ATTR_VALUE = MTL.INVENTORY_ITEM_ID "); 
			sb.append("AND  MTL.segment1 IN (");
			buildIDsList(inputList, sb);
			sb.append(" )) b ");
			sb.append("where  a.medicine_code =  b.MEDICINE_CODE )");
			sb.append("  ORDER BY  MEDICINE_CODE " );
			
			
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
