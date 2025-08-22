package org.tools.web.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ThreadFileSanitizer {

  public void createNewFileWithThreadLineAndStatusFromNextLineIntoSameLine(List<String> allLines) throws IOException {
    Iterator<String> iterator = allLines.iterator();
    List<String> buffer = new ArrayList<>();

    while (iterator.hasNext()) {
      String line = iterator.next();

      if (line.contains("elapsed=")) {

        String next = null;
        if (iterator.hasNext()) {
          String threadStatusLine = iterator.next();

          if (threadStatusLine.contains("java.lang.Thread.State")) {
            next = threadStatusLine;
          }
        }

        if (StringUtils.isNotBlank(next)) {
          buffer.add(line + " " + next);
        } else {
          buffer.add(line);
        }

      }
    }

    String outputFile = "./parsed.txt";
    FileUtils.writeLines(new File(outputFile), buffer);
  }

}
