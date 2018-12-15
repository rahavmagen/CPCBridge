package lists;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class HealthcareProviders extends GetList 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 13L;

	public HealthcareProviders()  {
		super();
		
	}

	@Override
	public String getServiceName() {
		
		return "HealthcareProviders";
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
				item.put("HpNumber", checkNull(resultSet.getString("HP_NUMBER")));
				item.put("VendorId", checkNull(resultSet.getString("VENDOR_ID")));
				item.put("HpName", checkNull(resultSet.getString("HEALTH_PROVIDER")));
				item.put("District", checkNull(resultSet.getString("district")));
				item.put("TariffType", checkNull(resultSet.getString("Tariff_type")));
				item.put("EffectiveFrom", checkNull(resultSet.getString("start_date_active")));
				item.put("EffectiveTo", checkNull(resultSet.getString("end_date_active")));
				item.put("Email", "");
				item.put("PrescribingLevel", "");
				item.put("AuthorizationNumber", "");
				item.put("HPCode", "");
				item.put("HpId", "");
				item.put("HealthProviderLevel", "");
				item.put("HospitalLevel", "");
				item.put("HospitalType", "");
				
				 /** TODO add values below */ 
				
				item.put("Region", "");
				item.put("OwnershipType", "");
				item.put("CateringService", "");
				item.put("AuthorizationNumber", "");
				item.put("IsDeleted", "");
				item.put("VendorSiteId", "");
				
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("HpNumber", "");
			item.put("HpName", "");
			item.put("District", "");			
			item.put("TariffType", "");
			item.put("EffectiveFrom", "");
			item.put("EffectiveTo", "");
			item.put("Email", "");
			item.put("PrescribingLevel", "");
			item.put("AuthorizationNumber", "");
			item.put("HPCode", "");
			item.put("HpId", "");
			item.put("HealthProviderLevel", "");
			item.put("HospitalLevel", "");
			item.put("HospitalType", "");
			item.put("Region", "");
			item.put("OwnershipType", "");
			item.put("CateringService", "");
			item.put("AuthorizationNumber", "");
			item.put("IsDeleted", "");
			item.put("VendorSiteId", "");
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
		sb.append("SELECT AP.VENDOR_ID, AP.ATTRIBUTE15 district, QPH.name Tariff_type,  AP.SEGMENT1 HP_NUMBER , AP.VENDOR_NAME HEALTH_PROVIDER , to_char(ap.end_date_active,'dd/MM/yyyy') end_date_active  ,");
		sb.append(" to_char(ap.start_date_active , 'dd/MM/yyyy') start_date_active ");
		sb.append("from  FND_FLEX_VALUE_SETS FFVS, FND_FLEX_VALUES FFV,QP_LIST_HEADERS_TL QPH ,  AP_SUPPLIERS AP ");
		sb.append("WHERE AP.ATTRIBUTE_CATEGORY = 'NHIA HP' ");
		sb.append("and   FFVS.FLEX_VALUE_SET_ID = FFV.FLEX_VALUE_SET_ID ");
		sb.append("AND FFVS.FLEX_VALUE_SET_NAME = 'NHIA_HEALTH_PROVIDER_TYPE' ");
		sb.append("AND FFV.FLEX_VALUE = ap.attribute1 ");
		sb.append("and AP.ATTRIBUTE_CATEGORY = 'NHIA HP' ");
		sb.append("and QPH.List_Header_Id = FFV.Attribute2 " );
		   
		switch(inputList.getBy)
		{
		case "All":
				
			  break;
			
		case "Modified":
			
			sb.append("AND AP.last_update_date between to_date('");
			sb.append(inputList.modifiedFrom );
			sb.append("','dd/MM/yyyy') and to_date('");
			sb.append(inputList.modifiedTo);
			sb.append("','dd/MM/yyyy')");
			
			break;
			
		case "Current":
			
			sb.append("AND NVL(ap.end_date_active, SYSDATE) >= SYSDATE ");
			

			break;
				
		case "Ids":
			
			sb.append("AND  ap.SEGMENT1 IN (");
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

			sb.append(")");
			break;
			
		default: 

			ediUtils.writeToLog("***********'getBy' input field is "+inputList.getBy+" it should be All or Modified or Current or Ids************");
			return null;
		}
		
		sb.append(" ORDER BY HP_NUMBER ");
		
		
		return sb.toString();
	}



}
