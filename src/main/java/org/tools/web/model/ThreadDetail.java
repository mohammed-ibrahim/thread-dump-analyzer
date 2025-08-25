package org.tools.web.model;


import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "ThreadDetail", indexes = {
    @Index(columnList = "id", name = "tdump_id"),
    @Index(columnList = "file_id", name = "file_identifier")})
public class ThreadDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "priority")
  private Integer priority;

  @Column(name = "os_priority")
  private Integer osPriority;

  @Column(name = "thread_number")
  private Integer threadNumber;

  @Column(name = "cpu")
  private Float cpu;

  @Column(name = "elapsed")
  private Float elapsed;

  @Column(name = "tid")
  private String tid;

  @Column(name = "nid")
  private String nid;

  @Column(name = "address")
  private String address;

  @Column(name = "waiting_condition")
  private String waitingCondition;

  @Column(name = "thread_state")
  private String threadState;

  @Column(name = "pool_name")
  private String poolName;

  @Column(name = "batch_number")
  private String batchNumber;

  @Column(name = "file_id")
  private String fileIdentifier;

  @Column(name = "hostname")
  private String hostname;

  @Column(name = "process_id")
  private Integer processID;

  @Column(name = "service_name")
  private String serviceName;

  @Column(name = "dump_date")
  private Date dumpDate;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getThreadNumber() {
    return threadNumber;
  }

  public void setThreadNumber(Integer threadNumber) {
    this.threadNumber = threadNumber;
  }

  public Float getCpu() {
    return cpu;
  }

  public void setCpu(Float cpu) {
    this.cpu = cpu;
  }

  public Float getElapsed() {
    return elapsed;
  }

  public void setElapsed(Float elapsed) {
    this.elapsed = elapsed;
  }

  public String getTid() {
    return tid;
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public String getNid() {
    return nid;
  }

  public void setNid(String nid) {
    this.nid = nid;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getOsPriority() {
    return osPriority;
  }

  public void setOsPriority(Integer osPriority) {
    this.osPriority = osPriority;
  }

  public String getWaitingCondition() {
    return waitingCondition;
  }

  public void setWaitingCondition(String waitingCondition) {
    this.waitingCondition = waitingCondition;
  }

  public String getThreadState() {
    return threadState;
  }

  public void setThreadState(String threadState) {
    this.threadState = threadState;
  }

  public String getPoolName() {
    return poolName;
  }

  public void setPoolName(String poolName) {
    this.poolName = poolName;
  }

  public String getBatchNumber() {
    return batchNumber;
  }

  public void setBatchNumber(String batchNumber) {
    this.batchNumber = batchNumber;
  }

  public String getFileIdentifier() {
    return fileIdentifier;
  }

  public void setFileIdentifier(String fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Integer getProcessID() {
    return processID;
  }

  public void setProcessID(Integer processID) {
    this.processID = processID;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public Date getDumpDate() {
    return dumpDate;
  }

  public void setDumpDate(Date dumpDate) {
    this.dumpDate = dumpDate;
  }
}

