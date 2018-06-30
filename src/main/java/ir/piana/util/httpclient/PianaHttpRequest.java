package ir.piana.util.httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;

import java.util.*;

public class PianaHttpRequest {
    private String baseUrl;
    private String resourcePath;
    private Map<String, String> pathParamMap = new LinkedHashMap<>();
    private Map<String, List<String>> queryParamsMap = new LinkedHashMap<>();
    private Map<String, Collection<String>> headersMap = new LinkedHashMap<>();
    private Object entity;
    private boolean isUtf8;
    private PianaRestMethod restMethod;

    private PianaHttpRequest(String baseUrl, PianaRestMethod restMethod) {
        this.baseUrl = baseUrl;
        this.restMethod = restMethod;
    }

    public static PianaHttpRequest getNewInstance(
            String baseUrl, PianaRestMethod restMethod) {
        return new PianaHttpRequest(baseUrl, restMethod);
    }

    public PianaHttpRequest setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    public PianaHttpRequest addPathParam(
            String key, String value)
            throws PianaHttpClientException {
        if(pathParamMap.containsKey(key))
            throw new PianaHttpClientException(
                    PianaRestException.DUPLICATE_PATH_PARAM,
                    "this key already exist!");
        pathParamMap.put(key, value);
        return this;
    }

    public PianaHttpRequest addQueryParams(String key, String value) {
        if(queryParamsMap.containsKey(key)) {
            queryParamsMap.get(key).add(value);
        } else {
            List<String> list = new ArrayList<>();
            list.add(value);
            queryParamsMap.put(key, list);
        }
        return this;
    }

    public PianaHttpRequest addHeaders(
            String key, String value) {
        if(headersMap.containsKey(key)) {
            headersMap.get(key).add(value);
        } else {
            Collection<String> list = new ArrayList<>();
            list.add(value);
            headersMap.put(key, list);
        }
        return this;
    }

    public PianaHttpRequest setEntity(
            Object entity, boolean isUtf8)
            throws PianaHttpClientException {
        if(restMethod != PianaRestMethod.POST
                && restMethod != PianaRestMethod.PUT) {
            throw new PianaHttpClientException(
                    PianaRestException.METHOD_NOT_SUPPORT_BODY,
                    "this method not support body");
        }
        if(entity != null) {
            this.isUtf8 = isUtf8;
            this.entity = entity;
        }
        return this;
    }

    public PianaHttpClient build()
            throws PianaHttpClientException {
        return new PianaHttpClient(
                PianaHttpRequestBuilder.buildRequest(this));
    }

    private static class PianaHttpRequestBuilder {
        static Request buildRequest(PianaHttpRequest httpRequest)
                throws PianaHttpClientException {
            PianaHttpRequestBuilder builder = new PianaHttpRequestBuilder();
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            AsyncHttpClient.BoundRequestBuilder requestBuilder = null;
            switch (httpRequest.restMethod) {
                case GET:
                    requestBuilder = asyncHttpClient
                            .prepareGet(builder.createUrl(httpRequest));
                    break;
                case POST:
                    requestBuilder = asyncHttpClient
                            .preparePost(builder.createUrl(httpRequest));
                    break;
                case PUT:
                    requestBuilder = asyncHttpClient
                            .preparePut(builder.createUrl(httpRequest));
                    break;
                case DELETE:
                    requestBuilder = asyncHttpClient
                            .prepareDelete(builder.createUrl(httpRequest));
                    break;
            }

            builder.setQueryParams(httpRequest, requestBuilder);
            builder.setHeaders(httpRequest, requestBuilder);
            builder.setEntity(httpRequest, requestBuilder);

            return requestBuilder.build();
        }

        private String createUrl(PianaHttpRequest httpRequest) {
            String url = "";
            String path = httpRequest.resourcePath;
            if(path.contains("{")) {
                url = path.substring(0, path.indexOf("{"));
                String next = path;
                do {
                    String pathParamName = next.substring(
                            next.indexOf("{") + 1, next.indexOf("}"));
                    String pathParamValue = httpRequest.pathParamMap
                            .get(pathParamName);
                    if(pathParamValue == null)
                        throw new RuntimeException();
                    url = url.concat(pathParamValue);
                    next = next.substring(next.indexOf("}") + 1);
                    if(next.contains("{")) {
                        String beforeCurved = next.substring(
                                0, next.indexOf('{'));
                        url += beforeCurved.isEmpty() ? "/" : beforeCurved;
                    }
                } while (next.contains("{"));
                if(url.charAt(url.length() - 1) == '/') {
                    url = url.substring(0, url.length() - 1);
                }
                url = url.concat(next);
            } else {
                url = path;
            }
            if(url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.lastIndexOf('/'));
            }

            String baseUrl = httpRequest.baseUrl;
            if(baseUrl.charAt(baseUrl.length() - 1) == '/' && url.charAt(0) == '/') {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            } else if(baseUrl.charAt(baseUrl.length() - 1) != '/' && url.charAt(0) != '/') {
                baseUrl = baseUrl.concat("/");
            }
            return baseUrl.concat(url);
        }

        private void setQueryParams(
                PianaHttpRequest httpRequest,
                BoundRequestBuilder boundRequestBuilder) {
            if(httpRequest.queryParamsMap != null) {
                boundRequestBuilder.setQueryParams(httpRequest.queryParamsMap);
//            for (String qpName : queryParams.keySet()) {
//                Collection<String> strings = queryParams.get(qpName);
//                for(String value : strings) {
//                    boundRequestBuilder.addQueryParam(qpName, value);
//                }
//            }
            }
        }

        private void setHeaders(
                PianaHttpRequest httpRequest,
                BoundRequestBuilder requestBuilder) {
            if(httpRequest.headersMap != null) {
                requestBuilder.setHeaders(httpRequest.headersMap);
            }
        }

        private void setEntity(
                PianaHttpRequest httpRequest,
                BoundRequestBuilder boundRequestBuilder) throws PianaHttpClientException {
            if(httpRequest.entity != null) {
                if(httpRequest.isUtf8)
                    boundRequestBuilder.addHeader(
                            "content-type", "application/json;charset=utf-8");
                else
                    boundRequestBuilder.addHeader(
                            "content-type", "application/json");
                String body = null;
                try {
                    body = new ObjectMapper()
                            .writeValueAsString(httpRequest.entity);
                } catch (JsonProcessingException e) {
                    throw new PianaHttpClientException(
                            PianaRestException.JSON_PROCESSING_EXCEPTION, e);
                }
                boundRequestBuilder.setBody(body);
            }
        }
    }
    
}
