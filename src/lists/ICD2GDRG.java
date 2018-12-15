package lists;

import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class ICD2GDRG  extends GetList 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7L;

	public ICD2GDRG()  {
		super();
		
	}

	@Override
	public String getServiceName() {
		
		return "ICD2GDRG";
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
				item.put("GDRGCode", checkNull(resultSet.getString("GDRG_CODE")));
				item.put("ICDCategoryCode", checkNull(resultSet.getString("ICD_CODE")));
				item.put("ItemDescription", checkNull(resultSet.getString("ITEM_DESCRIPTION")));  
				item.put("LevelOfCare", "");  
				item.put("Status", "Success");
				listArr.put(item);
			}


		} 
		catch (Exception e) 
		{
			JSONObject item = new JSONObject();
			item.put("GDRGCode", "");
			item.put("ICDCategoryCode", "");
			item.put("ItemDescription", "");  
			item.put("LevelOfCare", "");  
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
				
			sb.append("SELECT   (SELECT DISTINCT B.SEGMENT1 " );
			sb.append("FROM  MTL_SYSTEM_ITEMS_B B  ");
			sb.append("WHERE B.INVENTORY_ITEM_ID = T.INVENTORY_ITEM_ID) GDRG_CODE, " );
			sb.append("T.MFG_PART_NUM ICD_CODE, " );
			sb.append("T.ITEM_DESCRIPTION " );
			sb.append("FROM  MTL_MFG_PART_NUMBERS_ALL_V T " );
			
			break;
			
		case "Modified":
			
			sb.append("SELECT   (SELECT DISTINCT B.SEGMENT1 " );
			sb.append("FROM  MTL_SYSTEM_ITEMS_B B  ");
			sb.append("WHERE B.INVENTORY_ITEM_ID = T.INVENTORY_ITEM_ID) GDRG_CODE, " );
			sb.append("T.MFG_PART_NUM ICD_CODE, " );
			sb.append("T.ITEM_DESCRIPTION " );
			sb.append("FROM  MTL_MFG_PART_NUMBERS_ALL_V T " );
			sb.append("WHERE last_update_date between to_date('");
			sb.append(inputList.modifiedFrom );
			sb.append("','dd/MM/yyyy') and to_date('");
			sb.append(inputList.modifiedTo);
			sb.append("','dd/MM/yyyy')");
			
			break;
			
		case "Current":
				
			sb.append("SELECT   (SELECT DISTINCT B.SEGMENT1 " );
			sb.append("FROM  MTL_SYSTEM_ITEMS_B B  ");
			sb.append("WHERE B.INVENTORY_ITEM_ID = T.INVENTORY_ITEM_ID  ");
			sb.append("AND B.inventory_item_status_code = 'Active' ) GDRG_CODE, ");
			sb.append("T.MFG_PART_NUM ICD_CODE, " );
			sb.append("T.ITEM_DESCRIPTION " );
			sb.append("FROM  MTL_MFG_PART_NUMBERS_ALL_V T " );
			
				
			break;
				
		case "Ids":
			
			
			sb.append("SELECT   (SELECT DISTINCT B.SEGMENT1 " );
			sb.append("FROM  MTL_SYSTEM_ITEMS_B B  ");
			sb.append("WHERE B.INVENTORY_ITEM_ID = T.INVENTORY_ITEM_ID ) GDRG_CODE, " );
			sb.append("T.MFG_PART_NUM ICD_CODE, " );
			sb.append("T.ITEM_DESCRIPTION " );
			sb.append("FROM  MTL_MFG_PART_NUMBERS_ALL_V T " );
			sb.append("WHERE  T.MFG_PART_NUM IN (");
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
		
		sb.append(" ORDER BY T.INVENTORY_ITEM_ID, T.MFG_PART_NUM " ); 
		
		
		return sb.toString();
	}



}
