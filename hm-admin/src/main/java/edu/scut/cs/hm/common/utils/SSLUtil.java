package edu.scut.cs.hm.common.utils;

import com.google.common.collect.ImmutableList;
import org.springframework.util.Assert;

import javax.net.ssl.*;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SSLUtil {
    private SSLUtil() {

    }

    public static void disable() {
        try {
            SSLContext sslc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagerArray = {new NullX509TrustManager()};
            sslc.init(null, trustManagerArray, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static X509TrustManager combineX509TrustManagers(TrustManager[] ... tmses) {
        List<X509TrustManager> list = new ArrayList<>();
        for(TrustManager[] tms : tmses) {
            for(TrustManager tm : tms) {
                if(tm instanceof X509TrustManager) {
                    list.add((X509TrustManager) tm);
                }
            }
        }
        return new CombinedTrustManager(list);
    }

    public static class NullX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class CombinedTrustManager implements X509TrustManager {
        private final List<X509TrustManager> list;

        CombinedTrustManager(List<X509TrustManager> list) {
            this.list = ImmutableList.copyOf(list);
            Assert.notEmpty(this.list, "List of TrustManagers is empty");
        }

        private void checkTrusted(Func func) throws CertificateException {
            CertificateException ex = null;
            for (int i =0; i < list.size(); ++i) {
                X509TrustManager tm = list.get(i);
                try {
                    func.apply(tm);
                    // accepted
                    return;
                } catch (CertificateException e) {
                    if(ex == null || Throwables.has(e, CertPathValidatorException.class)) {
                        ex = e;
                    }
                }
            }
            if(ex != null) {
                throw ex;
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            checkTrusted(tm -> tm.checkClientTrusted(chain, authType));
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            checkTrusted(tm -> tm.checkServerTrusted(chain, authType));
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            List<X509Certificate> certs = new ArrayList<>();
            list.forEach(tm -> {
                certs.addAll(Arrays.asList(tm.getAcceptedIssuers()));
            });
            return certs.toArray(new X509Certificate[certs.size()]);
        }

        private interface Func {
            void apply(X509TrustManager tm) throws CertificateException;
        }
    }
}
