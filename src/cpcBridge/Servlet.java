package cpcBridge;

import java.io.IOException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;

import ap.PaymentNotification;
import ap.PaymentOrder;
import ap.PaymentRequest;
import lists.GDRG;
import lists.GDRGTariffs;
import lists.HealthcareProviders;
import lists.ICD10;
import lists.ICD2GDRG;
import lists.MedicineTariffs;
import lists.Medicines;
import members.GdrgTariffService;
import members.MedicineTariffService;
import members.MemberDetails;
import members.VerifyMembership;

public  class Servlet  <T> extends HttpServlet{

	
	private static final long serialVersionUID = 30L;
	
	
	public void	doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		response.setContentType("text/plain");
		String className = this.getClass().getName();
		response.getWriter().println("This is a test for WS for GET for "+className);
			
	}
	
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String responseStr = null;
		CpcBridgeAbstract<T> cpcAbstract = null;
		System.out.println("calling "+request.getRequestURI());
		try 
		{
			if(request.getRequestURI().contains("VerifyMembership"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new VerifyMembership();
				responseStr  = cpcAbstract.mainFlow(request);
			}

			else if(request.getRequestURI().contains("MemberDetails"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new MemberDetails();
				responseStr  = cpcAbstract.mainFlow(request);
			}

			else if(request.getRequestURI().contains("GdrgTariffService"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new GdrgTariffService();
				responseStr  = cpcAbstract.mainFlow(request);
			}

			else if(request.getRequestURI().contains("MedicineTariffService"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new MedicineTariffService();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			
			else if(request.getRequestURI().contains("Lists/ICD10"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new ICD10();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("Lists/GDRGTariffs"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new GDRGTariffs();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("Lists/GDRG"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new GDRG();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("Lists/ICD2GDRG"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new ICD2GDRG();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("Lists/Medicines"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new Medicines();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			
			else if(request.getRequestURI().contains("Lists/MedicineTariffs"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new MedicineTariffs();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("Lists/HealthcareProviders"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new HealthcareProviders();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("PaymentRequest"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new PaymentRequest();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("PaymentNotification"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new PaymentNotification();
				responseStr  = cpcAbstract.mainFlow(request);
			}
			else if(request.getRequestURI().contains("PaymentOrder"))
			{
				cpcAbstract = (CpcBridgeAbstract<T>) new PaymentOrder();
				responseStr  = cpcAbstract.mainFlow(request);
			}

			
			response.setContentType("text/plain");
			response.getWriter().println(responseStr);
		}
		catch (IOException | SQLException | NamingException | JSONException e) 
		{

			e.printStackTrace();
			responseStr = cpcAbstract.buildErrorReponse(cpcAbstract.getRequestId());
			try 
			{
				response.getWriter().println(responseStr);
				cpcAbstract.ediUtils.connection.close();
				cpcAbstract.oraAppUtils.connection.close();
			} catch (IOException | SQLException e1) 
			{
				
				
			}
		}
	}
}

