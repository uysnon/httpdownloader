package com.avgorkin.httpdownloader.model.datahandlers;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Data
@NoArgsConstructor
@Builder
public class HttpConnectionUtils {
    public HttpURLConnection buildConnection(String address, int connectionTimeoutMs, int readTimeoutMs){
        URL url = null;
        try {
            url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectionTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            return connection;
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }

    }
}
