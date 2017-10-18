package com.binance;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.BasicResponseHandler;


/**
 * An extension to BasicResponseHandler that helps identify errors by including the entity in
 * the Exception message.
 * 
 * Created by stephen on 10/17/17.
 */
public class HttpResponseHandler extends BasicResponseHandler {
    
    @Override
    public String handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();
        final String strEntity = entity == null ? null : handleEntity(entity); 
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(),
                    statusLine.getReasonPhrase() + " - " + strEntity);
        }
        return strEntity;
    }
}
