package org.sopt.server.adapter.out;

import java.util.concurrent.atomic.AtomicLong;
import org.sopt.server.application.port.out.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class SequenceIdGenerator implements IdGenerator {
  private final AtomicLong sequence = new AtomicLong();

  @Override
  public long nextId() {
    return sequence.incrementAndGet();
  }
}
