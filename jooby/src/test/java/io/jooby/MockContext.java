package io.jooby;

import io.jooby.internal.UrlParser;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class MockContext implements Context {

  private String method = Router.GET;

  private Route route;

  private String pathString;

  private Map<String, String> pathMap;

  private QueryString query = UrlParser.queryString("?");

  private String queryString;

  private Value.Object headers = Value.headers();

  private Formdata formdata = new Formdata();

  private Multipart multipart = new Multipart();

  private Body body;

  private Map<String, Object> locals = new HashMap<>();

  private Map<String, Parser> parsers = new HashMap<>();

  private boolean ioThread;

  private Map<String, Object> responseHeaders = new HashMap<>();

  private long length;
  private MediaType responseType;
  private Charset responseCharset = StandardCharsets.UTF_8;
  private StatusCode statusCode = StatusCode.OK;
  private Object result;
  private boolean responseStarted;

  @Nonnull @Override public String method() {
    return method;
  }

  public MockContext setMethod(String method) {
    this.method = method;
    return this;
  }

  @Nonnull @Override public Route route() {
    return route;
  }

  public MockContext route(Route route) {
    this.route = route;
    return this;
  }

  @Nonnull @Override public String pathString() {
    return pathString;
  }

  public MockContext setPathString(String pathString) {
    this.pathString = pathString;
    return this;
  }

  @Nonnull @Override public Map<String, String> pathMap() {
    return pathMap;
  }

  public MockContext setPathMap(Map<String, String> pathMap) {
    this.pathMap = pathMap;
    return this;
  }

  @Nonnull @Override public QueryString query() {
    return query;
  }

  @Nonnull @Override public String queryString() {
    return queryString;
  }

  public MockContext setQueryString(String queryString) {
    this.queryString = queryString;
    this.query = UrlParser.queryString("?" + queryString);
    return this;
  }

  @Nonnull @Override public Value headers() {
    return headers;
  }

  public MockContext setHeaders(Value.Object headers) {
    this.headers = headers;
    return this;
  }

  @Nonnull @Override public Formdata form() {
    return formdata;
  }

  public MockContext setForm(Formdata formdata) {
    this.formdata = formdata;
    return this;
  }

  @Nonnull @Override public Multipart multipart() {
    return multipart;
  }

  public void setMultipart(Multipart multipart) {
    this.multipart = multipart;
  }

  @Nonnull @Override public Body body() {
    return body;
  }

  public MockContext setBody(Body body) {
    this.body = body;
    return this;
  }

  public MockContext setBody(String body) {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    return setBody(bytes);
  }

  public MockContext setBody(byte[] body) {
    this.body = Body.of(new ByteArrayInputStream(body), body.length);
    return this;
  }

  @Nonnull @Override public Parser parser(@Nonnull MediaType contentType) {
    return parsers.getOrDefault(contentType, Parser.UNSUPPORTED_MEDIA_TYPE);
  }

  @Override public boolean isInIoThread() {
    return ioThread;
  }

  public MockContext setIoThread(boolean ioThread) {
    this.ioThread = ioThread;
    return this;
  }

  @Nonnull @Override public MockContext dispatch(@Nonnull Runnable action) {
    action.run();
    return this;
  }

  @Nonnull @Override
  public MockContext dispatch(@Nonnull Executor executor, @Nonnull Runnable action) {
    action.run();
    return this;
  }

  @Nonnull @Override public MockContext detach(@Nonnull Runnable action) {
    action.run();
    return null;
  }

  @Nullable @Override public <T> T attribute(String name) {
    return (T) locals.get(name);
  }

  @Nonnull @Override public MockContext attribute(@Nonnull String name, @Nonnull Object value) {
    locals.put(name, value);
    return this;
  }

  @Nonnull @Override public Map<String, Object> attributes() {
    return locals;
  }

  @Nonnull @Override public MockContext setHeader(@Nonnull String name, @Nonnull Date value) {
    Context.super.setHeader(name, value);
    return this;
  }

  @Nonnull @Override public MockContext setHeader(@Nonnull String name, @Nonnull Instant value) {
    Context.super.setHeader(name, value);
    return this;
  }

  @Nonnull @Override public MockContext setHeader(@Nonnull String name, @Nonnull Object value) {
    Context.super.setHeader(name, value);
    return this;
  }

  @Nonnull @Override public MockContext setHeader(@Nonnull String name, @Nonnull String value) {
    responseHeaders.put(name, value);
    return this;
  }

  @Nonnull @Override public MockContext setContentLength(long length) {
    this.length = length;
    return this;
  }

  public long getResponseLength() {
    return length;
  }

  @Nonnull @Override
  public MockContext setContentType(@Nonnull MediaType contentType, @Nullable Charset charset) {
    this.responseType = contentType;
    this.responseCharset = charset;
    return this;
  }

  @Nonnull @Override public MockContext statusCode(int statusCode) {
    this.statusCode = StatusCode.valueOf(statusCode);
    return this;
  }

  @Nonnull @Override public StatusCode statusCode() {
    return statusCode;
  }

  @Nonnull @Override public MockContext render(@Nonnull Object result) {
    this.result = result;
    return this;
  }

  public Object getResult() {
    return result;
  }

  public String getResultText() {
    return (String) result;
  }

  @Nonnull @Override public OutputStream responseStream() {
    responseStarted = true;
    ByteArrayOutputStream out = new ByteArrayOutputStream(Server._16KB);
    result = out;
    return out;
  }

  @Nonnull @Override public Sender responseSender() {
    return new Sender() {
      @Override public Sender sendBytes(@Nonnull byte[] data, @Nonnull Callback callback) {
        result = data;
        callback.onComplete(MockContext.this, null);
        return this;
      }

      @Override public void close() {

      }
    };
  }

  @Nonnull @Override public String remoteAddress() {
    return "0.0.0.0";
  }

  @Nonnull @Override public String protocol() {
    return "HTTP/1.1";
  }

  @Nonnull @Override public PrintWriter responseWriter(MediaType type, Charset charset) {
    responseStarted = true;
    setContentType(type, charset);
    PrintWriter writer = new PrintWriter(new StringWriter());
    result = writer;
    return writer;
  }

  @Nonnull @Override public MockContext sendString(@Nonnull String data, @Nonnull Charset charset) {
    responseStarted = true;
    result = data;
    length = data.getBytes(charset).length;
    return this;
  }

  @Nonnull @Override public MockContext sendBytes(@Nonnull byte[] data) {
    responseStarted = true;
    result = data;
    length = data.length;
    return this;
  }

  @Nonnull @Override public Context sendBytes(@Nonnull ByteBuf data) {
    responseStarted = true;
    result = data;
    length = data.readableBytes();
    return this;
  }

  @Nonnull @Override public MockContext sendBytes(@Nonnull ByteBuffer data) {
    result = data;
    length = data.remaining();
    return this;
  }

  @Nonnull @Override public Context sendStream(InputStream input) {
    responseStarted = true;
    result = input;
    return this;
  }

  @Nonnull @Override public Context sendFile(@Nonnull FileChannel file) {
    responseStarted = true;
    result = file;
    return this;
  }

  @Nonnull @Override public MockContext sendStatusCode(int statusCode) {
    responseStarted = true;
    result = statusCode;
    this.statusCode = StatusCode.valueOf(statusCode);
    length = 0;
    return this;
  }

  @Nonnull @Override public MockContext sendError(@Nonnull Throwable cause) {
    responseStarted = true;
    result = cause;
    length = -1;
    return this;
  }

  @Nonnull @Override public Context setDefaultContentType(@Nonnull MediaType contentType) {
    if (responseType == null) {
      responseType = contentType;
      responseCharset = contentType.charset();
    }
    return this;
  }

  @Nonnull @Override public MediaType responseContentType() {
    return responseType == null ? MediaType.text : responseType;
  }

  public Map<String, Object> getResponseHeaders() {
    return responseHeaders;
  }

  public Charset getResponseCharset() {
    return responseCharset;
  }

  @Override public boolean isResponseStarted() {
    return responseStarted;
  }

  @Override public String name() {
    return "mock";
  }

  @Nonnull @Override public Router router() {
    throw new UnsupportedOperationException();
  }
}