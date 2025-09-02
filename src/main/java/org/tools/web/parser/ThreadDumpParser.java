package org.tools.web.parser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tools.web.model.ThreadDetail;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ThreadDumpParser {

  private String[] splitAfterSecondQuote(String input) {
    int firstQuote = input.indexOf('"');
    if (firstQuote == -1) return new String[]{input, ""};

    int secondQuote = input.indexOf('"', firstQuote + 1);
    if (secondQuote == -1) return new String[]{input, ""};

    // Split after second quote
    String before = input.substring(0, secondQuote + 1);
    String after = input.substring(secondQuote + 1);

    return new String[]{before, after};
  }

  private String trimThreadNameQuotes(String threadName) {
    if (threadName != null && threadName.length() >= 2 &&
        threadName.startsWith("\"") && threadName.endsWith("\"")) {
      return threadName.substring(1, threadName.length() - 1);
    }
    return threadName; // return unchanged if no surrounding quotes
  }

  public ThreadDetail parseThreadLineV2(String line) {

    ThreadDetail threadDetail = new ThreadDetail();
    String[] parts = splitAfterSecondQuote(line);
    String threadName = trimThreadNameQuotes(parts[0]);
    threadDetail.setName(threadName);

    String subsequentPart = parts[1];

    List<String> subsequentParts = Arrays.asList(subsequentPart.split(" "));

    threadDetail.setThreadNumber(null);
    threadDetail.setThreadNumber(getThreadNumber(subsequentParts));
    threadDetail.setPriority(getTokenWithKeyAsInteger("prio=", subsequentParts, false));
    threadDetail.setOsPriority(getTokenWithKeyAsInteger("os_prio=", subsequentParts, false));
    threadDetail.setCpu(parseTimeFieldToMilliseconds("cpu=", subsequentParts));
    threadDetail.setElapsed(parseTimeFieldToMilliseconds("elapsed=", subsequentParts));
    threadDetail.setTid(getTokenWithKey("tid=", subsequentParts, false));
    threadDetail.setNid(getTokenWithKey("nid=", subsequentParts, false));
    threadDetail.setWaitingCondition(getWaitingCondition(line));
    threadDetail.setThreadState(getThreadState(line));
    threadDetail.setPoolName(getPoolName(threadDetail.getName()));

    return threadDetail;
  }

  public String getPoolName(String name) {
    if (StringUtils.startsWithIgnoreCase(name, "ForkJoin")) {
      int index = name.indexOf("-");
      return name.substring(0, index + 2);
    }

    if (StringUtils.contains(name, "@")) {
      return name.substring(0, name.indexOf("@"));
    }

    if (StringUtils.startsWithIgnoreCase(name, "GC") ||
        StringUtils.startsWithIgnoreCase(name, "VM") ||
        StringUtils.startsWithIgnoreCase(name, "DestroyJavaVM")) {
      return name;
    }

    if (StringUtils.startsWithIgnoreCase(name, "C") &&
        StringUtils.containsIgnoreCase(name, "CompilerThread")) {
      return "CompilerThread";
    }

    if (StringUtils.containsIgnoreCase(name, "-")) {
      String [] parts = name.split("-");
      String lastPart = parts[parts.length - 1];

      try {
        Integer.parseInt(lastPart);
      } catch (NumberFormatException e) {
        return name;
      }

      return name.substring(0, name.length() - lastPart.length() - 1);
    }

    return name;
  }

  private String getThreadState(String line) {
    String matchingCondition = "java.lang.Thread.State";
    int index = line.indexOf(matchingCondition);

    if (index >= 0) {
      String lineBeyondMatchingPattern = line.substring(index , line.length());
      return reduceMultipleSpaces(lineBeyondMatchingPattern);
    }

    return null;
  }

  private String reduceMultipleSpaces(String input) {
    if (input == null) return null;
    return input.replaceAll("\\s{2,}", " "); // replaces 2 or more whitespace chars with one space
  }

  private String getWaitingCondition(String line) {
    line = reduceMultipleSpaces(line);

    String condition = "waiting on condition";
    int index = line.indexOf(condition);

    if (index == -1) {
      condition = "runnable";
      index = line.indexOf(condition);
    }

    if (index == -1) {
      condition = "Object.wait()";
      index = line.indexOf(condition);
    }

    if (index >= 0) {
      String startingFromCondition = line.substring(index , line.length());
      int endIndex = startingFromCondition.indexOf(" ", condition.length() + 2);

      if (endIndex == -1) {
        return null;
      }
      return startingFromCondition.substring(0, endIndex);
    }

    return null;
  }

  private Integer getThreadNumber(List<String> subsequentParts) {
    Optional<String> first = subsequentParts.stream().filter(a -> a.startsWith("#")).findFirst();

    if (first.isPresent()) {
      String number = first.get().substring(1);
      return Integer.parseInt(number);
    }

    return null;
  }

  private String getTokenWithKey(String key, List<String> parts, boolean removeAlphaCharacters) {
    Optional<String> first = parts.stream().filter(a -> a.startsWith(key)).findFirst();
    if (first.isPresent()) {
      String firstToken = first.get();
      String processed = firstToken.substring(key.length());

      if (removeAlphaCharacters) {
        processed = removeAlphabets(processed);
      }

      return processed;
    }

    return null;
  }

  private Integer getTokenWithKeyAsInteger(String key, List<String> parts, boolean removeAlphaCharacters) {
    String value = getTokenWithKey(key, parts, removeAlphaCharacters);

    if (value != null) {
      return Integer.parseInt(value);
    }

    return null;
  }

  private Float parseTimeFieldToMilliseconds(String key, List<String> parts) {
    String value = getTokenWithKey(key, parts, false);

    Float factor = null;
    if (value.endsWith("ms")) {
      factor = 1f;
    } else if (value.endsWith("s")) {
      factor = 1000f;
    } else {
      throw new RuntimeException("CPU time cannot be parsed: " + value);
    }

    value = removeAlphabets(value);
    Float timeFieldInMilliSeconds = Float.parseFloat(value) * factor;
    return timeFieldInMilliSeconds;
  }

  private Float getTokenWithKeyAsFloat(String key, List<String> parts, boolean removeAlphaCharacters) {
    String value = getTokenWithKey(key, parts, removeAlphaCharacters);

    if (value != null) {
      return Float.parseFloat(value);
    }

    return null;
  }

  private String removeAlphabets(String input) {
    if (input == null) return null;
    return input.replaceAll("[a-zA-Z]", "");
  }

  public List<ThreadDetail> threadLinesToThreadDetails(List<String> lines) {
    return lines.stream().map(line -> parseThreadLineV2(line)).collect(Collectors.toList());
  }
}
