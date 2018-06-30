package ir.piana.util.httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PianaHttpClient {
    private Request request;

    PianaHttpClient(Request request) {
        this.request = request;
    }

    public Future<Response> execute() {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        return asyncHttpClient.executeRequest(request);
    }

    public Response execute(Integer timeout, TimeUnit timeUnit)
            throws PianaHttpClientException {
        try {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            if(timeout == null || timeout == 0)
                return asyncHttpClient.executeRequest(request)
                        .get();
            else
                return asyncHttpClient.executeRequest(request)
                        .get(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new PianaHttpClientException(
                    PianaRestException.INTERRUPTED_EXCEPTION, e);
        } catch (ExecutionException e) {
            throw new PianaHttpClientException(
                    PianaRestException.EXECUTION_EXCEPTION, e);
        } catch (TimeoutException e) {
            throw new PianaHttpClientException(
                    PianaRestException.TIMEOUT_EXCEPTION, e);
        }
    }

    public PianaHttpStatus getStatus(Response response)
            throws PianaHttpClientException {
        return PianaHttpStatus
                .fromStatusCode(response.getStatusCode());
    }

    public Map<String, Object> getBodyAsMap(Response response)
            throws PianaHttpClientException {
        Map<String, Object> responseMap = null;
        try {
            responseMap = new ObjectMapper().readValue(
                    response.getResponseBody(), LinkedHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseMap;
    }

    public Map<String, List<String>> getHeaderAsMap(
            Response response) {
        return response.getHeaders();
    }

    public void print(Response response) {
        if(response != null) {
            System.out.println(response.getStatusCode() + " - "
                    + response.getStatusText());
            Map<String, Object> map = null;
            try {
                map = new ObjectMapper().readValue(
                        response.getResponseBody(), LinkedHashMap.class);
                for(String key : map.keySet()) {
                    System.out.println(key + " => " + map.get(key));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
