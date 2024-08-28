package org.xxdc.oss.example.interop.benchmark;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.xxdc.oss.example.interop.PlayerIds;

@SuppressWarnings("unused")
public class PlayerIdsBenchmark {

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testPlayerIdsGetId() {
    PlayerIds ids = new PlayerIds(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.getNextId();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testPlayerIdsGetAndIncrementId() {
    PlayerIds ids = new PlayerIds(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.getNextIdAndIncrement();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testAtomicIntegerGetId() {
    AtomicInteger ids = new AtomicInteger(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.get();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testAtomicIntegerGetAndIncrement() {
    AtomicInteger ids = new AtomicInteger(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.getAndIncrement();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testLockGetId() {
    Control ids = new Control(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.getId();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void testLockGetAndIncrement() {
    Control ids = new Control(1);
    for (int i = 0; i < 1000; i++) {
      var id = ids.getAndIncrement();
    }
  }

  /// Naive implementation of an id generator.
  private static class Control {
    private final ReentrantLock lock = new ReentrantLock();

    private int id = 1;

    private Control(int initialValue) {
      this.id = initialValue;
    }

    private int getId() {
      lock.lock();
      try {
        return id;
      } finally {
        lock.unlock();
      }
    }

    private int getAndIncrement() {
      lock.lock();
      try {
        int oldId = id;
        id = id + 1;
        return oldId;
      } finally {
        lock.unlock();
      }
    }
  }
}
