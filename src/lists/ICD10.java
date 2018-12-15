package lists;

import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class ICD10 extends GetList 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ICD10()  {
		super();
		
	}

	@Override
	public String getServiceName() {
		
		return "ICD10";
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
				item.put("Id", checkNull(resultSet.getString("ICD10_code")));
				item.put("ICD10Description", checkNull(resultSet.getString("description")));
				item.put("EffectiveFrom", checkNull(resultSet.getString("start_date_active")));
				item.put("EffectiveTo", checkNull(resultSet.getString("end_date_active")));
				item.put("IsDeleted", ""); /** TODO populate the is deleted with the right value  */
				item.put("ChapterNumber", "");
				item.put("BlockCodesSpan", "");
				item.put("SuperCategoryCode", "");
				
				
				item.put("BlockTitle", ""); /** those are empty */  
				item.put("ChapterTitle", "");
				item.put("BlockCodesSpan", "");
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("Id", "");
			item.put("ICD10Description", "");
			item.put("EffectiveFrom", "");
			item.put("EffectiveTo", "");
			item.put("IsDeleted", "");
			item.put("ChapterNumber", "");
			item.put("BlockCodesSpan", "");
			item.put("SuperCategoryCode", "");
			item.put("BlockTitle", "");
			item.put("ChapterTitle", "");
			item.put("BlockCodesSpan", "");
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
		sb.append("SELECT DISTINCT segment1 ICD10_code, description , start_date_active, end_date_active " );  /** TODO add the is deleted and  block title */
		sb.append("FROM  INV.MTL_SYSTEM_ITEMS_B  ");
		sb.append("WHERE item_type = 'NHIA ICD10' " );   
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
