package ir.piana.util.httpclient;

import com.ning.http.client.Response;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PianaHttpClientTest {
    @BeforeClass
    public static void beforeClass() {

    }

//    @Test
    public void test()
            throws PianaHttpClientException,
            ExecutionException,
            InterruptedException {

        PianaHttpClient client = PianaHttpRequest.getNewInstance(
                "http://host:port/{base_url}", PianaRestMethod.GET)
                .setResourcePath(
                        "part_1/{path_param_1}/part_2/{path_param_2}")
                .addPathParam("path_param_1", "param_1")
                .addPathParam("path_param_2", "param_2")
                .addHeaders("Authorization", "Basic YWRtaW46MTIz") // base64 => admin:123
                .addQueryParams("query_param_1", "value_1")
                .addQueryParams("query_param_2", "value_2").build();
        Response response = client.execute(0, TimeUnit.SECONDS);
        client.print(response);
        Assert.assertEquals("status not equal to 200",
                client.getStatus(response), PianaHttpStatus.OK);
    }
}
