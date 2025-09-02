package org.tools.web.parser;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tools.web.diskops.DumpFileDetails;
import org.tools.web.model.ThreadDetail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ThreadDumpFileOrchestrator {

  private ThreadDumpParser threadDumpParser;

  private ThreadFileSanitizer threadFileSanitizer;

  @Autowired
  public ThreadDumpFileOrchestrator(ThreadDumpParser threadDumpParser, ThreadFileSanitizer threadFileSanitizer) {
    this.threadDumpParser = threadDumpParser;
    this.threadFileSanitizer = threadFileSanitizer;
  }

  public List<ThreadDetail> convertThreadDumpFileToThreadList(File threadDumpFile, DumpFileDetails dumpFileDetails, String zipFileName) {
    try {
      List<String> lines = FileUtils.readLines(threadDumpFile, StandardCharsets.UTF_8);
      List<String> sanitizedLines = this.threadFileSanitizer.sanitiseThreadDumpToParsableLines(lines);
      List<ThreadDetail> threadDetails = this.threadDumpParser.threadLinesToThreadDetails(sanitizedLines);
      applyDumpDetails(threadDetails, dumpFileDetails, zipFileName);
      return threadDetails;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private void applyDumpDetails(List<ThreadDetail> threadDetails, DumpFileDetails dumpFileDetails, String zipFileName) {
    for (ThreadDetail threadDetail : threadDetails) {
      threadDetail.setHostname(dumpFileDetails.getHostname());
      threadDetail.setProcessID(dumpFileDetails.getProcessId());
      threadDetail.setDumpDate(dumpFileDetails.getFileDate());
      threadDetail.setServiceName(dumpFileDetails.getJarName());
      threadDetail.setFileIdentifier(dumpFileDetails.getFileName());
      threadDetail.setBatchNumber(zipFileName);
    }
  }
}
