package org.tools.web.parser;

import org.testng.annotations.Test;
import org.tools.web.model.ThreadDetail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class ThreadDiffCalculatorTest {

  @Test
  public void canDetectDeletedThreads() {
    ThreadDetail threada = getThreadDetail("1", 11.1F, 100.3F, "pool1");
    ThreadDetail threadb = getThreadDetail("1", 11.1F, 100.3F, "pool1");
    ThreadDetail threadc = getThreadDetail("2", 11.1F, 100.3F, "pool2");
    ThreadDetail threadd = getThreadDetail("3", 11.1F, 100.3F, "pool3");

    ThreadDiffCalculator threadDiffCalculator = new ThreadDiffCalculator();
    List<ThreadDetail> oldSnapShot = Arrays.asList(threada, threadc);
    List<ThreadDetail> newSnapShot = Arrays.asList(threadb, threadd);
    List<String> strings = threadDiffCalculator.printIfCpuNotIncreased(oldSnapShot, newSnapShot, toMap(oldSnapShot), toMap(newSnapShot));
    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "1");

    strings = threadDiffCalculator.printIfElapsedNotIncreased(oldSnapShot, newSnapShot, toMap(oldSnapShot), toMap(newSnapShot));
    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "1");


    List<String> deletedThreads = threadDiffCalculator.printDeletedThreads(oldSnapShot, newSnapShot, toMap(oldSnapShot), toMap(newSnapShot));
    assertEquals(deletedThreads.size(), 1);
    assertEquals(deletedThreads.get(0), "2");

    List<String> addedThreads = threadDiffCalculator.printAddedThreads(oldSnapShot, newSnapShot, toMap(oldSnapShot), toMap(newSnapShot));
    assertEquals(addedThreads.size(), 1);
    assertEquals(addedThreads.get(0), "3");
  }

  private Map<String, ThreadDetail> toMap(List<ThreadDetail> snapShot) {
    return snapShot.stream().collect(Collectors.toMap(ThreadDetail::getNid, t -> t));
  }

  private ThreadDetail getThreadDetail(String nid, Float cpu, Float elapsed, String poolName) {
    ThreadDetail threadDetail = new ThreadDetail();
    threadDetail.setNid(nid);
    threadDetail.setCpu(cpu);
    threadDetail.setElapsed(elapsed);
    threadDetail.setPoolName(poolName);
    return threadDetail;
  }
}