package io.jooby.internal.handler;

import io.jooby.Context;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ContextSubscriber implements Subscriber<Object> {

  private final Context ctx;

  public ContextSubscriber(Context ctx) {
    this.ctx = ctx;
  }

  @Override public void onSubscribe(Subscription s) {
    s.request(Long.MAX_VALUE);
  }

  @Override public void onNext(Object value) {
    ctx.send(value);
  }

  @Override public void onError(Throwable x) {
    ctx.sendError(x);
  }

  @Override public void onComplete() {
  }
}