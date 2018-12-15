package ap;

import java.util.ArrayList;

public class PaymentReqInput {
	
	public ArrayList<PaymentReq> paymentsReq = new ArrayList<>();
	String requestId;
	
	public class PaymentReq
	{
	String batchId;
	String healthFacilityCode;
	String tariffAmount;
	
	}
}
