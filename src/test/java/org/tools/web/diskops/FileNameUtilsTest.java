package org.tools.web.diskops;

import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

import static org.testng.Assert.*;

public class FileNameUtilsTest {

  @Test
  public void canExtractDumpFileDetails() {
    String filename = "56894-some-windows-server-112312-JavaServiceJar.jar---2025-08-25-06-57-55.txt";
    FileNameUtils fileNameUtils = new FileNameUtils();
    DumpFileDetails dumpFileDetails = fileNameUtils.getDumpFileDetails(filename);

    assertEquals(dumpFileDetails.getProcessId(), 56894);
    assertEquals(dumpFileDetails.getHostname(), "some-windows-server-112312");
    assertEquals(dumpFileDetails.getJarName(), "JavaServiceJar.jar");

    Date expectedDate = getDate(2025, Calendar.AUGUST, 25, 06, 57, 55);

    assertEquals(expectedDate, dumpFileDetails.getFileDate());

  }

  public Date getDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);

    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);

    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
  }

}