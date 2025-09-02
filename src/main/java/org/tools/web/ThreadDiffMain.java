package org.tools.web;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tools.web.diskops.DumpFileDetails;
import org.tools.web.diskops.FileNameUtils;
import org.tools.web.model.ThreadDetail;
import org.tools.web.parser.ThreadDiffCalculator;
import org.tools.web.parser.ThreadDumpFileOrchestrator;
import org.tools.web.ziputil.ZipExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
    reportDir.mkdirs();
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

    String dumpComparisonFilter = System.getProperty("tdump.compare.filename.filter");
    if (StringUtils.isBlank(dumpComparisonFilter)) {
      throw new RuntimeException("tdump.compare.filename.filter not set");
    }

    List<File> filteredFiles = filesInsideZip.stream().filter(f -> StringUtils.containsIgnoreCase(f.getName(), dumpComparisonFilter)).collect(Collectors.toList());
    List<File> sortedFilesByDumpTime = new ArrayList<>(filteredFiles);
    Collections.sort(sortedFilesByDumpTime, getThreadDetailComparator());

    try {
      FileUtils.writeLines(Paths.get(reportDir.toString(), "sorted.txt").toFile(), sortedFilesByDumpTime);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    List<String> buffer = new ArrayList<>();

    for (int i = 0; i < sortedFilesByDumpTime.size() - 1; i++) {
      File firstFile = sortedFilesByDumpTime.get(i);
      File secondFile = sortedFilesByDumpTime.get(i + 1);
      List<String> report = getReport(firstFile, secondFile, zipFile, reportDir);
      buffer.addAll(report);
    }


    try {
      Path path = Paths.get(reportDir.toString(), zipFile.getName() + "---report.txt");
      FileUtils.writeLines(path.toFile(), buffer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    this.zipExtractor.cleanup(filesInsideZip);
  }

  public List<String> getReport(File firstFile, File secondFile, File zipFile, File reportDir) {
    List<ThreadDetail> firstBatch = getThreadDetails(firstFile, zipFile.getName());
    List<ThreadDetail> secondBatch = getThreadDetails(secondFile, zipFile.getName());

    List<String> report = new ArrayList<>();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    report.add(String.format("\n\nComparing: %s ::: %s", firstFile.getName(), secondFile.getName()));
    List<String> diffResult = this.threadDiffCalculator.calculateDiff(firstBatch, secondBatch);
    report.addAll(diffResult);
    stopWatch.stop();

    System.out.println("Finished comparing: " + firstFile + " and " + secondFile + " in " + stopWatch.getTime());
    return report;
  }

  private List<ThreadDetail> getThreadDetails(File threadDumpFile, String zipFileName) {
    DumpFileDetails dumpFileDetails = fileNameUtils.getDumpFileDetails(threadDumpFile.getName());
    return this.threadDumpFileOrchestrator.convertThreadDumpFileToThreadList(threadDumpFile, dumpFileDetails, zipFileName);
  }

  public Comparator<File> getThreadDetailComparator() {
    return (file1, file2) -> {
      Date d1 = this.fileNameUtils.parseTimestampFromFilenameForTxtExtension(file1.getName());
      Date d2 = this.fileNameUtils.parseTimestampFromFilenameForTxtExtension(file2.getName());
      return d1.compareTo(d2);
    };
  }
}
