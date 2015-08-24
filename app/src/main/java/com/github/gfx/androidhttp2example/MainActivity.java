package com.github.gfx.androidhttp2example;

import com.cookpad.android.rxt4a.schedulers.AndroidSchedulers;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    OkHttpClient okHttpClient;

    TextView textView;

    final PublishSubject<String> subject = PublishSubject.create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.content);

        okHttpClient = new OkHttpClient();
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                            String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                            String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        try {
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        }
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        okHttpClient.setSslSocketFactory(sslSocketFactory);
        okHttpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        subject.subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        okHttpClient.newCall(new Request.Builder()
                .url("https://10.0.2.2:8081")
                .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        subject.onNext(e.toString());
                        Log.wtf("XXX", e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        subject.onNext(response.toString());
                        Log.d("XXX", response.toString());
                    }
                });
    }
}
