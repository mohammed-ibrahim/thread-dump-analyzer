package org.tools.web.parser;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import org.tools.web.model.ThreadDetail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class ThreadDumpParserTest {

  @Test
  public void testThreadDump() {
    ThreadDumpParser threadDumpParser = new ThreadDumpParser();
    String input = "\"AsyncListner background processing\" #327 daemon prio=5 os_prio=0 cpu=14953.13ms elapsed=424135.33s tid=0x000002096f866380 nid=0x178c waiting on condition  [0x000000a8adeff000]    java.lang.Thread.State: TIMED_WAITING (sleeping)";
    ThreadDetail threadDetail = threadDumpParser.parseThreadLineV2(input);
    assertEquals(threadDetail.getName(), "AsyncListner background processing");
    assertEquals(threadDetail.getThreadNumber(), 327);
    assertEquals(threadDetail.getPriority(), 5);
    assertEquals(threadDetail.getOsPriority(), 0);
    assertEquals(threadDetail.getCpu(), 14953.13f);
    assertEquals(threadDetail.getElapsed(), 424135.33f);
    assertEquals(threadDetail.getTid(), "0x000002096f866380");
    assertEquals(threadDetail.getNid(), "0x178c");
    assertEquals(threadDetail.getWaitingCondition(), "waiting on condition [0x000000a8adeff000]");
    assertEquals(threadDetail.getThreadState(), "java.lang.Thread.State: TIMED_WAITING (sleeping)");
  }
}