package org.tools.web.parser;

import org.springframework.stereotype.Component;
import org.tools.web.model.ThreadDetail;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class ThreadDiffCalculator {

  public void calculateDiff(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot) {

    Map<String, ThreadDetail> oldNidToThreadMapping = oldSnapShot.stream().collect(Collectors.toMap(ThreadDetail::getNid, t -> t));
    Map<String, ThreadDetail> newNidToThreadMapping = newSnapShot.stream().collect(Collectors.toMap(ThreadDetail::getNid, t -> t));

    printDeletedThreads(oldSnapShot, newSnapShot, oldNidToThreadMapping, newNidToThreadMapping);
    printAddedThreads(oldSnapShot, newSnapShot, oldNidToThreadMapping, newNidToThreadMapping);
    printIfCpuNotIncreased(oldSnapShot, newSnapShot, oldNidToThreadMapping, newNidToThreadMapping);
    printIfElapsedNotIncreased(oldSnapShot, newSnapShot, oldNidToThreadMapping, newNidToThreadMapping);
    printChangeInPoolSize(oldSnapShot, newSnapShot);
  }

  public List<String> printIfCpuNotIncreased(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot, Map<String, ThreadDetail> oldNidToThreadMapping, Map<String, ThreadDetail> newNidToThreadMapping) {
    Set<String> nids = new HashSet<>();
    nids.addAll(oldNidToThreadMapping.keySet());
    nids.retainAll(newNidToThreadMapping.keySet());
    List<String> nidsForWhichCpuDidntIncreased = new ArrayList<>();


    nids.forEach(nid -> {
      Float oldCpu = oldNidToThreadMapping.get(nid).getCpu();
      Float newCpu = newNidToThreadMapping.get(nid).getCpu();

      if (oldCpu.equals(newCpu)) {
        System.out.println(String.format("No Change in CPU: %s", oldNidToThreadMapping.get(nid).getName()));
        nidsForWhichCpuDidntIncreased.add(nid);
      }
    });

    return nidsForWhichCpuDidntIncreased;
  }

  public List<String> printIfElapsedNotIncreased(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot, Map<String, ThreadDetail> oldNidToThreadMapping, Map<String, ThreadDetail> newNidToThreadMapping) {
    Set<String> nids = new HashSet<>();
    nids.addAll(oldNidToThreadMapping.keySet());
    nids.retainAll(newNidToThreadMapping.keySet());
    List<String> nidsForWhichElapsedDidntIncreased = new ArrayList<>();

    nids.forEach(nid -> {
      Float oldElapsed = oldNidToThreadMapping.get(nid).getElapsed();
      Float newElapsed = newNidToThreadMapping.get(nid).getElapsed();

      if (oldElapsed.equals(newElapsed)) {
        System.out.println(String.format("No Change in ELAPSED: %s", oldNidToThreadMapping.get(nid).getName()));
        nidsForWhichElapsedDidntIncreased.add(nid);
      }
    });

    return nidsForWhichElapsedDidntIncreased;
  }

  private void printChangeInPoolSize(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot) {
    Map<String, Integer> oldPoolMap = new HashMap<>();

    oldSnapShot.forEach(t -> {
      Integer count = oldPoolMap.getOrDefault(t.getPoolName(), 0);
      oldPoolMap.put(t.getPoolName(), count + 1);
    });

    Map<String, Integer> newPoolMap = new HashMap<>();

    oldSnapShot.forEach(t -> {
      Integer count = newPoolMap.getOrDefault(t.getPoolName(), 0);
      newPoolMap.put(t.getPoolName(), count + 1);
    });

    Set<String> allKeys = new HashSet<>(oldPoolMap.keySet());
    allKeys.addAll(newPoolMap.keySet());

    AtomicBoolean changed = new AtomicBoolean(false);

    allKeys.forEach(key -> {

      if (oldPoolMap.containsKey(key) && newPoolMap.containsKey(key)) {
        Integer oldCount = oldPoolMap.get(key);
        Integer newCount = newPoolMap.get(key);

        if (!oldCount.equals(newCount)) {
          changed.set(true);
          System.out.println(String.format("Change of threads %s === %d => %d", key, oldCount, newCount));
        }
      }

      if (oldPoolMap.containsKey(key) && !newPoolMap.containsKey(key)) {
        System.out.println(String.format("Pool deleted: %s", key));
      }

      if (!oldPoolMap.containsKey(key) && newPoolMap.containsKey(key)) {
        System.out.println(String.format("Pool added: %s", key));
      }

    });

    if (!changed.get()) {
      System.out.println("No change in pool size");
    }
  }

  public List<String> printAddedThreads(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot, Map<String, ThreadDetail> oldNidToThreadMapping, Map<String, ThreadDetail> newNidToThreadMapping) {
    List<String> oldNids = oldSnapShot.stream().map(t -> t.getNid()).collect(Collectors.toList());
    List<String> newNids = newSnapShot.stream().map(t -> t.getNid()).collect(Collectors.toList());
    List<String> newThreads = new ArrayList<>();
    newNids.removeAll(oldNids);

    if (!newNids.isEmpty()) {
      System.out.println("Added threads");

      for (String nid : newNids) {
        ThreadDetail oldThread = newNidToThreadMapping.get(nid);
        System.out.println("Added: " + oldThread.getNid() + " " + oldThread.getName());
        newThreads.add(nid);
      }
    } else {
      System.out.println("No Threads were deleted");
    }

    return newThreads;
  }

  public List<String> printDeletedThreads(List<ThreadDetail> oldSnapShot, List<ThreadDetail> newSnapShot, Map<String, ThreadDetail> oldNidToThreadMapping, Map<String, ThreadDetail> newNidToThreadMapping) {
    List<String> oldNids = oldSnapShot.stream().map(t -> t.getNid()).collect(Collectors.toList());
    List<String> newNids = newSnapShot.stream().map(t -> t.getNid()).collect(Collectors.toList());
    List<String> deletedThreadNids = new ArrayList<>();
    oldNids.removeAll(newNids);

    if (!oldNids.isEmpty()) {
      System.out.println("Deleted threads");

      for (String nid : oldNids) {
        ThreadDetail oldThread = oldNidToThreadMapping.get(nid);
        System.out.println("Deleted: " + oldThread.getNid() + " " + oldThread.getName());
        deletedThreadNids.add(oldThread.getNid());
      }
    } else {
      System.out.println("No Threads were deleted");
    }

    return deletedThreadNids;
  }
}
