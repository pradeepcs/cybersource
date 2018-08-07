package org.csp.cybersource;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;

/**
 * Hello world!
 *
 */
public class JWTGenerator {

	public static void main(String[] args) throws Exception {
		System.out.print(getJWToken(
				"{\\\"clientReferenceInformation\\\":{\\\"code\\\":\\\"TC50171_3\\\"},\\\"processingInformation\\\":{\\\"commerceIndicator\\\":\\\"internet\\\"},\\\"orderInformation\\\":{\\\"billTo\\\":{\\\"country\\\":\\\"US\\\",\\\"lastName\\\":\\\"VDP\\\",\\\"address2\\\":\\\"Address 2\\\",\\\"address1\\\":\\\"201 S. Division St.\\\",\\\"postalCode\\\":\\\"48104-2201\\\",\\\"locality\\\":\\\"Ann Arbor\\\",\\\"administrativeArea\\\":\\\"MI\\\",\\\"firstName\\\":\\\"RTS\\\",\\\"phoneNumber\\\":\\\"999999999\\\",\\\"district\\\":\\\"MI\\\",\\\"buildingNumber\\\":\\\"123\\\",\\\"company\\\":\\\"Visa\\\",\\\"email\\\":\\\"test@cybs.com\\\"},\\\"amountDetails\\\":{\\\"totalAmount\\\":\\\"102.21\\\",\\\"currency\\\":\\\"USD\\\"}},\\\"paymentInformation\\\":{\\\"card\\\":{\\\"expirationYear\\\":\\\"2031\\\",\\\"number\\\":\\\"5555555555554444\\\",\\\"securityCode\\\":\\\"123\\\",\\\"expirationMonth\\\":\\\"12\\\",\\\"type\\\":\\\"002\\\"}}}"));
	}

	public static String getJWToken(String requestPayload) throws Exception {
		X509Certificate x509Certificate = initializeCertificate();
		RSAPrivateKey rsaPrivateKey = initializePrivateKey();
		JWSHeader jwsHeader = getJWTHeader(x509Certificate);
		Payload payload = getJWTPayload(requestPayload);
		JWSObject jwsObject = new JWSObject(jwsHeader, payload);
		signJWObject(jwsObject, rsaPrivateKey);
		return jwsObject.serialize();
	}

	private static String getPayloadAsJson(String requestBody) throws Exception {
		MessageDigest jwtBody = MessageDigest.getInstance("SHA-256");
		byte[] Headers = jwtBody.digest(requestBody.getBytes());
		String digest = java.util.Base64.getEncoder().encodeToString(Headers);
		String content = "{\"digest\":\"" + digest + "\",\"digestAlgorithm\":\"SHA-256\",\"iat\":\""
				+ DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))) + "\"}";
		return content;
	}

	private static X509Certificate initializeCertificate() throws Exception {
		KeyStore merchantKeyStore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
		merchantKeyStore.load(new FileInputStream(PaymentInterface.APP_PROP.getProperty("certificatePath")),
				PaymentInterface.APP_PROP.getProperty("secret").toCharArray());
		String merchantKeyAlias = null;
		Enumeration<String> enumKeyStore = merchantKeyStore.aliases();
		while (enumKeyStore.hasMoreElements()) {
			merchantKeyAlias = (String) enumKeyStore.nextElement();
			if (merchantKeyAlias.contains(PaymentInterface.APP_PROP.getProperty("secret"))) {
				break;
			}
		}
		PrivateKeyEntry keyEntry = (PrivateKeyEntry) merchantKeyStore.getEntry(merchantKeyAlias,
				new PasswordProtection(PaymentInterface.APP_PROP.getProperty("secret").toCharArray()));
		return (X509Certificate) keyEntry.getCertificate();
	}

	private static RSAPrivateKey initializePrivateKey() throws Exception {
		KeyStore merchantKeyStore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
		merchantKeyStore.load(new FileInputStream(PaymentInterface.APP_PROP.getProperty("certificatePath")),
				PaymentInterface.APP_PROP.getProperty("secret").toCharArray());
		String merchantKeyAlias = null;
		Enumeration<String> enumKeyStore = merchantKeyStore.aliases();
		while (enumKeyStore.hasMoreElements()) {
			merchantKeyAlias = (String) enumKeyStore.nextElement();
			if (merchantKeyAlias.contains(PaymentInterface.APP_PROP.getProperty("secret"))) {
				break;
			}
		}
		PrivateKeyEntry keyEntry = (PrivateKeyEntry) merchantKeyStore.getEntry(merchantKeyAlias,
				new PasswordProtection(PaymentInterface.APP_PROP.getProperty("secret").toCharArray()));
		return (RSAPrivateKey) keyEntry.getPrivateKey();
	}

	private static JWSHeader getJWTHeader(X509Certificate x509Certificate) throws CertificateEncodingException {
		HashMap<String, Object> customHeaders = new HashMap<String, Object>();
		customHeaders.put("v-c-merchant-id", PaymentInterface.APP_PROP.getProperty("merchantId"));
		String serialNumber = null;
		String serialNumberPrefix = "SERIALNUMBER=";
		String principal = x509Certificate.getSubjectDN().getName().toUpperCase();
		int beg = principal.indexOf(serialNumberPrefix);
		if (beg >= 0) {
			int x5cBase64List = principal.indexOf(",", beg);
			if (x5cBase64List == -1) {
				x5cBase64List = principal.length();
			}
			serialNumber = principal.substring(beg + serialNumberPrefix.length(), x5cBase64List);
		} else {
			serialNumber = x509Certificate.getSerialNumber().toString();
		}

		ArrayList<Base64> x5cBase64List1 = new ArrayList<Base64>();
		x5cBase64List1.add(Base64.encode(x509Certificate.getEncoded()));

		JWSHeader jwsHeader = (new com.nimbusds.jose.JWSHeader.Builder(JWSAlgorithm.RS256)).customParams(customHeaders)
				.keyID(serialNumber).x509CertChain(x5cBase64List1).build();
		return jwsHeader;
	}

	private static Payload getJWTPayload(String requestPayload) throws Exception {
		Payload payload = new Payload(getPayloadAsJson(requestPayload));
		return payload;
	}

	private static JWSObject signJWObject(JWSObject jwsObject, PrivateKey privateKey) {
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
		try {
			RSASSASigner joseException = new RSASSASigner(rsaPrivateKey);
			jwsObject.sign(joseException);
			if (!jwsObject.getState().equals(com.nimbusds.jose.JWSObject.State.SIGNED)) {
				return null;
			} else {
				return jwsObject;
			}
		} catch (JOSEException var15) {
			return null;
		}
	}

}
