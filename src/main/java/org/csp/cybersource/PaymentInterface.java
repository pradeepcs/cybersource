package org.csp.cybersource;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PaymentInterface {

	private static final String requestBody = "{\"clientReferenceInformation\":{\"code\":\"TC50171_3\"},\"processingInformation\":{\"commerceIndicator\":\"internet\"},\"orderInformation\":{\"billTo\":{\"country\":\"US\",\"lastName\":\"VDP\",\"address2\":\"Address 2\",\"address1\":\"201 S. Division St.\",\"postalCode\":\"48104-2201\",\"locality\":\"Ann Arbor\",\"administrativeArea\":\"MI\",\"firstName\":\"RTS\",\"phoneNumber\":\"999999999\",\"district\":\"MI\",\"buildingNumber\":\"123\",\"company\":\"Visa\",\"email\":\"test@cybs.com\"},\"amountDetails\":{\"totalAmount\":\"102.21\",\"currency\":\"USD\"}},\"paymentInformation\":{\"card\":{\"expirationYear\":\"2031\",\"number\":\"5555555555554444\",\"securityCode\":\"123\",\"expirationMonth\":\"12\",\"type\":\"002\"}}}";
	
	public static Properties APP_PROP = new Properties();
	
	static {
		APP_PROP.put("certificatePath", "d:/downloads/cspmid0001.p12");
		APP_PROP.put("secret", "cspmid0001");
		APP_PROP.put("merchantId", "cspmid0001");
		APP_PROP.put("baseURL", "https://apitest.cybersource.com/pts");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(authorize("/v2/payments", requestBody).readEntity(String.class));
	}

	public static Response authorize(String resourcePath, String req) throws Exception {
		return performPaymentOperation(resourcePath, req);
	}

	public static Response settlement(String resourcePath, String req) throws Exception {
		return performPaymentOperation(resourcePath, req);
	}
	
	public static Response refund(String resourcePath, String req) throws Exception {
		return performPaymentOperation(resourcePath, req);
	}
	
	public static Response authReversal(String resourcePath, String req) throws Exception {
		return performPaymentOperation(resourcePath, req);
	}

	private static Response performPaymentOperation(String resourcePath, String req) throws Exception {
		Client client = ClientBuilder.newClient();
		Response response = client.target(APP_PROP.getProperty("baseURL")+resourcePath).request()
				.header("Authorization", "Bearer "+JWTGenerator.getJWToken(req))
				.header("Content-Type", MediaType.APPLICATION_JSON)
				.post(Entity.entity(req, MediaType.APPLICATION_JSON));
		return response;
	}

	
}
