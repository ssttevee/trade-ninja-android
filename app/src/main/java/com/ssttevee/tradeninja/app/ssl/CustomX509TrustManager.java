package com.ssttevee.tradeninja.app.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CustomX509TrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {

		// Here you can verify the servers certificate. (e.g. against one which is stored on mobile device)

//		InputStream inStream = null;
//		try {
//			inStream = TradeNinjaApplication.loadCertAsInputStream();
//			CertificateFactory cf = CertificateFactory.getInstance("X.509");
//			X509Certificate ca = (X509Certificate)
//			cf.generateCertificate(inStream);
//			inStream.close();
//
//			for (X509Certificate cert : certs) {
//				// Verifing by public key
//				cert.verify(ca.getPublicKey());
//			}
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Untrusted Certificate!");
//		} finally {
//			try {
//				inStream.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}