package org.tools.web.diskops;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class FileNameUtils {


  public static final String SIMPLE_SEPARATOR = "-";
  public static final String TIMESTAMP_SEPARATOR = "---";

  private static final SimpleDateFormat FORMAT =
      new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  public Date parseTimestampFromFilenameForTxtExtension(String filename) {
    return parseTimestampFromFilenameWithExtension(filename, ".txt");
  }

  public Date parseTimestampFromFilenameForZipExtension(String filename) {
    return parseTimestampFromFilenameWithExtension(filename, ".zip");
  }

  public DumpFileDetails getDumpFileDetails(String filename) {
    int indexOfTsSeparator = filename.indexOf(TIMESTAMP_SEPARATOR);

    if (indexOfTsSeparator < 0) {
      throw new IllegalArgumentException("Invalid filename: " + filename);
    }

    String hostDetails = filename.substring(0, indexOfTsSeparator);

    String parts[] = hostDetails.split(SIMPLE_SEPARATOR);
    Integer processId = Integer.parseInt(parts[0]);

    List<String> buffer = new ArrayList<>();

    for (int i=1; i<parts.length-1; i++) {
      buffer.add(parts[i]);
    }
    String hostname = StringUtils.join(buffer, SIMPLE_SEPARATOR);
    String jarName = parts[parts.length-1];

    DumpFileDetails details = new DumpFileDetails();
    details.setFileName(filename);
    details.setProcessId(processId);
    details.setHostname(hostname);
    details.setJarName(jarName);
    details.setFileDate(parseTimestampFromFilenameForTxtExtension(filename));

    return details;
  }

  public Date parseTimestampFromFilenameWithExtension(String filename, String extension) {
    if (filename.endsWith(extension)) {
      filename = filename.substring(0, filename.length() - 4);
    }

    try {
      String[] parts = filename.split(TIMESTAMP_SEPARATOR);
      String timeStampPart = parts[parts.length - 1];

      if (timeStampPart.endsWith(extension)) {
        timeStampPart = timeStampPart.substring(0, timeStampPart.length() - 4);
      }

      return FORMAT.parse(timeStampPart);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Filename does not match required format: " + filename, e);
    }
  }

}
