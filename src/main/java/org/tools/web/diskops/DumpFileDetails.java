package org.tools.web.diskops;

import java.util.Date;

public class DumpFileDetails {

  private String fileName;

  private Integer processId;

  private String hostname;

  private String jarName;

  private Date fileDate;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Integer getProcessId() {
    return processId;
  }

  public void setProcessId(Integer processId) {
    this.processId = processId;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getJarName() {
    return jarName;
  }

  public void setJarName(String jarName) {
    this.jarName = jarName;
  }

  public Date getFileDate() {
    return fileDate;
  }

  public void setFileDate(Date fileDate) {
    this.fileDate = fileDate;
  }
}
