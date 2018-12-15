package lists;

import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;


public class GDRG extends GetList 
{

	private static final long serialVersionUID = 6L;

	public  GDRG() {
		super();

	}

	public String getServiceName() 
	{

		return "GDRG";
	}

	@Override
	public JSONArray buildList(ResultSet resultSet) 
	{

		ediUtils.startMethod();
		JSONArray listArr = new JSONArray();

		try    
		{

			while (resultSet.next())
			{
				JSONObject item = new JSONObject();
				item.put("Code", checkNull(resultSet.getString("Medicines")));
				item.put("Name", checkNull(resultSet.getString("Description")));
				/**TODO  check value of the health care*/
				item.put("HealthcareLevel", "");  
				item.put("EffectiveFrom", "");
				item.put("EffectiveTo", "");
				item.put("ICDCategoryList", "");
				item.put("IsDeleted", "");
				/** to here */ 
				
				item.put("CoreCode", "");
				item.put("MDCcode", "");
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("Code", "");
			item.put("Name", "");
			item.put("HealthcareLevel", "");
			item.put("EffectiveFrom", "");
			item.put("EffectiveTo", "");
			item.put("ICDCategoryList", "");
			item.put("IsDeleted", "");
			item.put("Status", "Failure");
			e.printStackTrace();
			listArr.put(item);
		}



		return listArr;	


	}


	public String buildSqlQuery(InputListValues inputList ) 
	{
		ediUtils.startMethod();
		ediUtils.writeToLog("getBy = "+inputList.getBy);
		StringBuilder  sb = new StringBuilder(); 
		sb.append("SELECT DISTINCT segment1 Medicines, description , start_date_active, end_date_active " );
		sb.append("FROM  INV.MTL_SYSTEM_ITEMS_B  ");
		sb.append("WHERE item_type = 'NHIA G-DRG' " );
		sb.append(" AND   segment1 LIKE '00%' ");
		switch(inputList.getBy)
		{
		case "All":

			break;

		case "Modified":

			sb.append("AND last_update_date between to_date('");
			sb.append(inputList.modifiedFrom );
			sb.append("','dd/MM/yyyy') and to_date('");
			sb.append(inputList.modifiedTo);
			sb.append("','dd/MM/yyyy')");

			break;

		case "Current":

			sb.append("AND inventory_item_status_code = 'Active'");

			break;

		case "Ids":

			sb.append("AND  segment1 IN (");
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

		sb.append(" ORDER BY segment1 ");


		return sb.toString();
		
	}

}