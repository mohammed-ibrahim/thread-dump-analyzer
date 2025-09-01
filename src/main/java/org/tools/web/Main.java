package org.tools.web;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tools.web.dbops.DbUtils;
import org.tools.web.diskops.DumpFileDetails;
import org.tools.web.diskops.FileNameUtils;
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

  private FileNameUtils fileNameUtils;

  private DbUtils dbUtils;

  @Autowired
  public Main(ZipExtractor zipExtractor,
              ThreadFileSanitizer threadFileSanitizer,
              ThreadDumpParser threadDumpParser,
              FileNameUtils fileNameUtils,
              DbUtils dbUtils) {
    this.zipExtractor = zipExtractor;
    this.threadDumpParser = threadDumpParser;
    this.threadFileSanitizer = threadFileSanitizer;
    this.fileNameUtils = fileNameUtils;
    this.dbUtils = dbUtils;
  }

  public void beingImport() {
    String directoryStr = System.getProperty("zipdump.directory");

    if (StringUtils.isBlank(directoryStr)) {
      throw new RuntimeException("zipdump.directory not set");
    }

    File directory = new File(directoryStr);
    File[] files = directory.listFiles(pathname -> pathname.getName().endsWith(".zip"));

    for (File zipFile : files) {

      List<ThreadDetail> existingThreadsLimitingTo10 = dbUtils.loadExistingThreadsByBatchNumber(zipFile.getName());

      if (existingThreadsLimitingTo10.size() > 0) {
        System.out.println("File is already imported: " + zipFile.getName());
      }

      List<File> filesInsideZip = this.zipExtractor.unZipToTempDirectory(zipFile);
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      for (File threadDumpFile : filesInsideZip) {
        DumpFileDetails dumpFileDetails = fileNameUtils.getDumpFileDetails(threadDumpFile.getName());
        List<ThreadDetail> threadDetails = processThreadDumpFile(threadDumpFile, dumpFileDetails, zipFile.getName());
        dbUtils.importFile(threadDetails, dumpFileDetails.getFileName());
      }

      stopWatch.stop();
      System.out.println("Completed zipfile: " + zipFile.getAbsolutePath() + " time: " + stopWatch.getTime() + " ms");
    }
  }

  private List<ThreadDetail> processThreadDumpFile(File threadDumpFile, DumpFileDetails dumpFileDetails, String zipFileName) {
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
