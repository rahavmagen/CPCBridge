package ap;

import java.util.ArrayList;

public class PaymentNotificationInput {
	
	String requestId;
	ArrayList<PaymentDetail> payments = new ArrayList<>();
	
	public class PaymentDetail 
	{
		String paymentOrder;
		String healthFacilityCode;
		String amount;

		ArrayList<Invoice> invoices = new ArrayList<>();

		public class Invoice 
		{

			String invoiceId;
			String batch;

		}

	}
	
}
