package org.csp.cybersource;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class PaymentInterfaceTest {

	//@Test
	public void testAuthorize() throws Exception {
		String resourcePath = "/pts/v2/payments";
		String req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"processingInformation\":{\"commerceIndicator\":\"internet\"},\"orderInformation\":{\"billTo\":{\"country\":\"US\",\"lastName\":\"VDP\",\"address2\":\"Address 2\",\"address1\":\"201 S. Division St.\",\"postalCode\":\"48104-2201\",\"locality\":\"Ann Arbor\",\"administrativeArea\":\"MI\",\"firstName\":\"RTS\",\"phoneNumber\":\"999999999\",\"district\":\"MI\",\"buildingNumber\":\"123\",\"company\":\"Visa\",\"email\":\"test@cybs.com\"},\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}},\"paymentInformation\":{\"card\":{\"expirationYear\":\"2031\",\"number\":\"5555555555554444\",\"securityCode\":\"123\",\"expirationMonth\":\"12\",\"type\":\"002\"}}}";
		Response response = PaymentInterface.authorize(resourcePath, req);
		assertEquals(201, response.getStatus());
	}

	//@Test
	public void testSettlement() throws Exception {
		String resourcePath = "/pts/v2/payments/5336445981176187204105/captures";
		String req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"orderInformation\":{\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}}}";
		Response response = PaymentInterface.settlement(resourcePath, req);
		assertEquals(201, response.getStatus());
	}

	@Test
	public void testAuthReversal() throws Exception {

		String resourcePath = "/pts/v2/payments";
		String req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"processingInformation\":{\"commerceIndicator\":\"internet\"},\"orderInformation\":{\"billTo\":{\"country\":\"US\",\"lastName\":\"VDP\",\"address2\":\"Address 2\",\"address1\":\"201 S. Division St.\",\"postalCode\":\"48104-2201\",\"locality\":\"Ann Arbor\",\"administrativeArea\":\"MI\",\"firstName\":\"RTS\",\"phoneNumber\":\"999999999\",\"district\":\"MI\",\"buildingNumber\":\"123\",\"company\":\"Visa\",\"email\":\"test@cybs.com\"},\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}},\"paymentInformation\":{\"card\":{\"expirationYear\":\"2031\",\"number\":\"5555555555554444\",\"securityCode\":\"123\",\"expirationMonth\":\"12\",\"type\":\"002\"}}}";
		Response response = PaymentInterface.authorize(resourcePath, req);
		if(response.getStatus() == 201) {
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(response.readEntity(String.class));
			resourcePath = JsonPath.read(document, "$._links.authReversal.href");
			
			req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"reversalInformation\":{\"reason\":\"testing\",\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}}}";
			response = PaymentInterface.authReversal(resourcePath, req);
			assertEquals(201, response.getStatus());
		}
	}

	@Test
	public void testRefund() throws Exception {
		String resourcePath = "/pts/v2/payments";
		String req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"processingInformation\":{\"commerceIndicator\":\"internet\"},\"orderInformation\":{\"billTo\":{\"country\":\"US\",\"lastName\":\"VDP\",\"address2\":\"Address 2\",\"address1\":\"201 S. Division St.\",\"postalCode\":\"48104-2201\",\"locality\":\"Ann Arbor\",\"administrativeArea\":\"MI\",\"firstName\":\"RTS\",\"phoneNumber\":\"999999999\",\"district\":\"MI\",\"buildingNumber\":\"123\",\"company\":\"Visa\",\"email\":\"test@cybs.com\"},\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}},\"paymentInformation\":{\"card\":{\"expirationYear\":\"2031\",\"number\":\"5555555555554444\",\"securityCode\":\"123\",\"expirationMonth\":\"12\",\"type\":\"002\"}}}";
		Response response = PaymentInterface.authorize(resourcePath, req);
		if(response.getStatus() == 201) {
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(response.readEntity(String.class));
			resourcePath = JsonPath.read(document, "$._links.capture.href");
		
			req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"orderInformation\":{\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}}}";
			response = PaymentInterface.settlement(resourcePath, req);
			if(response.getStatus() == 201) {
				document = Configuration.defaultConfiguration().jsonProvider().parse(response.readEntity(String.class));
				resourcePath = JsonPath.read(document, "$._links.refund.href");
				req = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"orderInformation\":{\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}}}";
				response = PaymentInterface.refund(resourcePath, req);
				assertEquals(201, response.getStatus());
			}
		}
	}
	
	
	
}
