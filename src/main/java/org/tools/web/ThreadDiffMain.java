package org.tools.web;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tools.web.diskops.DumpFileDetails;
import org.tools.web.diskops.FileNameUtils;
import org.tools.web.model.ThreadDetail;
import org.tools.web.parser.ThreadDiffCalculator;
import org.tools.web.parser.ThreadDumpFileOrchestrator;
import org.tools.web.parser.ThreadDumpParser;
import org.tools.web.ziputil.ZipExtractor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ThreadDiffMain {

  private ThreadDumpFileOrchestrator threadDumpFileOrchestrator;

  private ThreadDiffCalculator threadDiffCalculator;

  private ZipExtractor zipExtractor;

  private FileNameUtils fileNameUtils;

  @Autowired
  public ThreadDiffMain(ThreadDiffCalculator threadDiffCalculator,
                        ThreadDumpFileOrchestrator threadDumpFileOrchestrator,
                        ZipExtractor zipExtractor,
                        FileNameUtils fileNameUtils) {
    this.threadDumpFileOrchestrator = threadDumpFileOrchestrator;
    this.threadDiffCalculator = threadDiffCalculator;
    this.zipExtractor = zipExtractor;
    this.fileNameUtils = fileNameUtils;
  }

  public void start() {
    String reportsDirectory = System.getProperty("reports.directory");
    if (StringUtils.isBlank(reportsDirectory)) {
      throw new RuntimeException("reports.directory not set");
    }

    File reportDir = new File(reportsDirectory);
    String directoryStr = System.getProperty("zipdump.directory");

    if (StringUtils.isBlank(directoryStr)) {
      throw new RuntimeException("zipdump.directory not set");
    }

    File directory = new File(directoryStr);
    File[] files = directory.listFiles(pathname -> pathname.getName().endsWith(".zip"));

    for (File zipFile : files) {
      processZipFile(zipFile, reportDir);
    }
  }

  private void processZipFile(File zipFile, File reportDir) {
    List<File> filesInsideZip = this.zipExtractor.unZipToTempDirectory(zipFile);

    List<File> sortedFilesByDumpTime = new ArrayList<>(filesInsideZip);
    Collections.sort(sortedFilesByDumpTime, getThreadDetailComparator());
    sortedFilesByDumpTime.forEach(s -> System.out.println(s.getName()));


    for (int i = 0; i < filesInsideZip.size() - 1; i++) {
      File firstFile = filesInsideZip.get(i);
      File secondFile = filesInsideZip.get(i + 1);
      scanForAnomalies(firstFile, secondFile, zipFile, reportDir);
    }

    this.zipExtractor.cleanup(filesInsideZip);
  }

  public void scanForAnomalies(File firstFile, File secondFile, File zipFile, File reportDir) {
    List<ThreadDetail> firstBatch = getThreadDetails(firstFile, zipFile.getName());
    List<ThreadDetail> secondBatch = getThreadDetails(secondFile, zipFile.getName());
    this.threadDiffCalculator.calculateDiff(firstBatch, secondBatch);
  }

  private List<ThreadDetail> getThreadDetails(File threadDumpFile, String zipFileName) {
    DumpFileDetails dumpFileDetails = fileNameUtils.getDumpFileDetails(threadDumpFile.getName());
    return this.threadDumpFileOrchestrator.convertThreadDumpFileToThreadList(threadDumpFile, dumpFileDetails, zipFileName);
  }

  public Comparator<File> getThreadDetailComparator() {
    return (file1, file2) -> {
      Date d1 = this.fileNameUtils.parseTimestampFromFilenameForTxtExtension(file1.getName());
      Date d2 = this.fileNameUtils.parseTimestampFromFilenameForTxtExtension(file2.getName());
      return d2.compareTo(d1);
    };
  }
}
