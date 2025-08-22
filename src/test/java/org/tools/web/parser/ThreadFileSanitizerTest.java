package org.tools.web.parser;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class ThreadFileSanitizerTest {


  private ThreadFileSanitizer threadFileSanitizer = new ThreadFileSanitizer();

  @Test
  public void canSanitize() throws IOException {
    List<String> output = threadFileSanitizer.sanitiseThreadDumpToParsableLines(readLinesFromResource());
    assertEquals(output.size(), 37);
    List<String> linesWithThreadState = output.stream().filter(t -> t.contains("java.lang.Thread.State")).collect(Collectors.toList());
    assertEquals(linesWithThreadState.size(), 31);
  }

  private List<String> readLinesFromResource() throws IOException {
    String string = IOUtils.toString(this.getClass().getResourceAsStream("/thread-dump-1.txt"));
    string = string.replace("\r\n", "\n");
    return Arrays.asList(string.split("\n"));
  }

}