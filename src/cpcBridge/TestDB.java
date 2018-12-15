package cpcBridge;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edi.nhia.com.EDIUtils;

public class TestDB extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String SYSTEM_NAME = "CPCTestDB" ;
	public final static String USER_NAME = "rahav" ;
	static EDIUtils utils;
	
	public TestDB () throws SQLException
	{
		utils = new EDIUtils(SYSTEM_NAME, USER_NAME  );
		utils.setWriteLogToLogFile(EDIUtils.c_debug_level_debug);
		
	}
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	
	utils.startMethod();
	response.setContentType("application/json");
	
	String dbDate = null;
	try {
		utils.initDB();
		ResultSet resultSet =  utils.executeSqlQuery("select sysdate from dual");
		
		 if (resultSet.next() )
		 {
			 dbDate = resultSet.getString("sysdate");
			 
		 }
	} 
	catch (Exception e) 
	{
		
		response.getWriter().println("{ \"Status\" : \"Failure\"}");
		e.printStackTrace();
		return;
	}
	
	response.getWriter().println("{ \"DBDate\" : \""+dbDate +"}");
	
	}
	
	
}
