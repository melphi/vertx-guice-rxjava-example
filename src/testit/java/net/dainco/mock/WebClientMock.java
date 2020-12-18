package net.dainco.mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import java.util.List;

public class WebClientMock {
  private WebClientMock() {
    // Intentionally empty.
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final WebClient webClient;

    public Builder() {
      this.webClient = mock(WebClient.class);
    }

    public Builder whenGet(String uri, byte[] bytes) {
      when(webClient.get(eq(uri))).thenReturn(HttpRequestMock.create(bytes));
      return this;
    }

    public WebClient build() {
      return webClient;
    }
  }

  private static class HttpResponseMock implements HttpResponse<Buffer> {
    private byte[] data;

    public HttpResponseMock(byte[] data) {
      this.data = data;
    }

    @Override
    public HttpVersion version() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public int statusCode() {
      return 200;
    }

    @Override
    public String statusMessage() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public MultiMap headers() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public @Nullable String getHeader(String headerName) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public MultiMap trailers() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public @Nullable String getTrailer(String trailerName) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public List<String> cookies() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public Buffer body() {
      return Buffer.buffer(data);
    }

    @Override
    public @Nullable Buffer bodyAsBuffer() {
      return Buffer.buffer(data);
    }

    @Override
    public List<String> followedRedirects() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public @Nullable JsonArray bodyAsJsonArray() {
      throw new IllegalArgumentException("Not yet implemented.");
    }
  }

  private static class HttpRequestMock implements HttpRequest<Buffer> {
    private final byte[] responseBody;

    private HttpRequestMock(byte[] responseBody) {
      this.responseBody = responseBody;
    }

    public static HttpRequestMock create(byte[] responseBody) {
      return new HttpRequestMock(responseBody);
    }

    @Override
    public HttpRequest<Buffer> method(HttpMethod value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> rawMethod(String method) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> port(int value) {
      return this;
    }

    @Override
    public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public HttpRequest<Buffer> host(String value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> virtualHost(String value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> uri(String value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> putHeaders(MultiMap headers) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> putHeader(String name, String value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> putHeader(String name, Iterable<String> value) {
      return this;
    }

    @Override
    public MultiMap headers() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public HttpRequest<Buffer> basicAuthentication(String id, String password) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> basicAuthentication(Buffer id, Buffer password) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> bearerTokenAuthentication(String bearerToken) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> ssl(Boolean value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> timeout(long value) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> addQueryParam(String paramName, String paramValue) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> setQueryParam(String paramName, String paramValue) {
      return this;
    }

    @Override
    public HttpRequest<Buffer> followRedirects(boolean value) {
      return null;
    }

    @Override
    public HttpRequest<Buffer> expect(ResponsePredicate predicate) {
      return this;
    }

    @Override
    public MultiMap queryParams() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public HttpRequest<Buffer> copy() {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public HttpRequest<Buffer> multipartMixed(boolean allow) {
      return this;
    }

    @Override
    public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void sendJson(@Nullable Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      throw new IllegalArgumentException("Not yet implemented.");
    }

    @Override
    public void send(Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
      handler.handle(new AsyncResult<>() {
        @Override
        public HttpResponse<Buffer> result() {
          return new HttpResponseMock(responseBody);
        }

        @Override
        public Throwable cause() {
          return null;
        }

        @Override
        public boolean succeeded() {
          return true;
        }

        @Override
        public boolean failed() {
          return false;
        }
      });
    }
  }
}
