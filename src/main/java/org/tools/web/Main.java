package org.tools.web;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tools.web.model.ThreadDetail;
import org.tools.web.parser.ThreadDumpParser;
import org.tools.web.parser.ThreadFileSanitizer;
import org.tools.web.ziputil.ZipExtractor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class Main {

  private ZipExtractor zipExtractor;

  private ThreadDumpParser threadDumpParser;

  private ThreadFileSanitizer threadFileSanitizer;

  @Autowired
  public Main(ZipExtractor zipExtractor, ThreadFileSanitizer threadFileSanitizer, ThreadDumpParser threadDumpParser) {
    this.zipExtractor = zipExtractor;
    this.threadDumpParser = threadDumpParser;
    this.threadFileSanitizer = threadFileSanitizer;
  }

  public void beingImport() {
    String directoryStr = System.getProperty("zipdump.directory");
    File directory = new File(directoryStr);
    File[] files = directory.listFiles(pathname -> pathname.getName().endsWith(".zip"));

    for (File zipFile : files) {
      List<File> filesInsideZip = this.zipExtractor.unZipToTempDirectory(zipFile);
      for (File threadDumpFile : filesInsideZip) {
        processThreadDumpFile(threadDumpFile);
      }
    }
  }

  private List<ThreadDetail> processThreadDumpFile(File threadDumpFile) {
    try {

      List<String> lines = FileUtils.readLines(threadDumpFile, StandardCharsets.UTF_8);
      List<String> sanitizedLines = this.threadFileSanitizer.sanitiseThreadDumpToParsableLines(lines);
      List<ThreadDetail> threadDetails = this.threadDumpParser.threadLinesToThreadDetails(sanitizedLines);
      return threadDetails;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
