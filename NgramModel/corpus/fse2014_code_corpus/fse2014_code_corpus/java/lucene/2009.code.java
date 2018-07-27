package org.apache.lucene.store;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
public class MockRAMDirectory extends RAMDirectory {
  long maxSize;
  long maxUsedSize;
  double randomIOExceptionRate;
  Random randomState;
  boolean noDeleteOpenFile = true;
  boolean preventDoubleWrite = true;
  private Set<String> unSyncedFiles;
  private Set<String> createdFiles;
  volatile boolean crashed;
  Map<String,Integer> openFiles;
  private synchronized void init() {
    if (openFiles == null)
      openFiles = new HashMap<String,Integer>();
    if (createdFiles == null)
      createdFiles = new HashSet<String>();
    if (unSyncedFiles == null)
      unSyncedFiles = new HashSet<String>();
  }
  public MockRAMDirectory() {
    super();
    init();
  }
  public MockRAMDirectory(Directory dir) throws IOException {
    super(dir);
    init();
  }
  public void setPreventDoubleWrite(boolean value) {
    preventDoubleWrite = value;
  }
  @Override
  public synchronized void sync(String name) throws IOException {
    maybeThrowDeterministicException();
    if (crashed)
      throw new IOException("cannot sync after crash");
    if (unSyncedFiles.contains(name))
      unSyncedFiles.remove(name);
  }
  public synchronized void crash() throws IOException {
    crashed = true;
    openFiles = new HashMap<String,Integer>();
    Iterator<String> it = unSyncedFiles.iterator();
    unSyncedFiles = new HashSet<String>();
    int count = 0;
    while(it.hasNext()) {
      String name = it.next();
      RAMFile file = fileMap.get(name);
      if (count % 3 == 0) {
        deleteFile(name, true);
      } else if (count % 3 == 1) {
        final int numBuffers = file.numBuffers();
        for(int i=0;i<numBuffers;i++) {
          byte[] buffer = file.getBuffer(i);
          Arrays.fill(buffer, (byte) 0);
        }
      } else if (count % 3 == 2) {
        file.setLength(file.getLength()/2);
      }
      count++;
    }
  }
  public synchronized void clearCrash() throws IOException {
    crashed = false;
  }
  public void setMaxSizeInBytes(long maxSize) {
    this.maxSize = maxSize;
  }
  public long getMaxSizeInBytes() {
    return this.maxSize;
  }
  public long getMaxUsedSizeInBytes() {
    return this.maxUsedSize;
  }
  public void resetMaxUsedSizeInBytes() {
    this.maxUsedSize = getRecomputedActualSizeInBytes();
  }
  public void setNoDeleteOpenFile(boolean value) {
    this.noDeleteOpenFile = value;
  }
  public boolean getNoDeleteOpenFile() {
    return noDeleteOpenFile;
  }
  public void setRandomIOExceptionRate(double rate, long seed) {
    randomIOExceptionRate = rate;
    randomState = new Random(seed);
  }
  public double getRandomIOExceptionRate() {
    return randomIOExceptionRate;
  }
  void maybeThrowIOException() throws IOException {
    if (randomIOExceptionRate > 0.0) {
      int number = Math.abs(randomState.nextInt() % 1000);
      if (number < randomIOExceptionRate*1000) {
        throw new IOException("a random IOException");
      }
    }
  }
  @Override
  public synchronized void deleteFile(String name) throws IOException {
    deleteFile(name, false);
  }
  private synchronized void deleteFile(String name, boolean forced) throws IOException {
    maybeThrowDeterministicException();
    if (crashed && !forced)
      throw new IOException("cannot delete after crash");
    if (unSyncedFiles.contains(name))
      unSyncedFiles.remove(name);
    if (!forced) {
      if (noDeleteOpenFile && openFiles.containsKey(name)) {
        throw new IOException("MockRAMDirectory: file \"" + name + "\" is still open: cannot delete");
      }
    }
    super.deleteFile(name);
  }
  @Override
  public synchronized IndexOutput createOutput(String name) throws IOException {
    if (crashed)
      throw new IOException("cannot createOutput after crash");
    init();
    if (preventDoubleWrite && createdFiles.contains(name) && !name.equals("segments.gen"))
      throw new IOException("file \"" + name + "\" was already written to");
    if (noDeleteOpenFile && openFiles.containsKey(name))
      throw new IOException("MockRAMDirectory: file \"" + name + "\" is still open: cannot overwrite");
    RAMFile file = new RAMFile(this);
    if (crashed)
      throw new IOException("cannot createOutput after crash");
    unSyncedFiles.add(name);
    createdFiles.add(name);
    RAMFile existing = fileMap.get(name);
    if (existing!=null && !name.equals("segments.gen") && preventDoubleWrite)
      throw new IOException("file " + name + " already exists");
    else {
      if (existing!=null) {
        sizeInBytes.getAndAdd(-existing.sizeInBytes);
        existing.directory = null;
      }
      fileMap.put(name, file);
    }
    return new MockRAMOutputStream(this, file, name);
  }
  @Override
  public synchronized IndexInput openInput(String name) throws IOException {
    RAMFile file = fileMap.get(name);
    if (file == null)
      throw new FileNotFoundException(name);
    else {
      if (openFiles.containsKey(name)) {
        Integer v =  openFiles.get(name);
        v = Integer.valueOf(v.intValue()+1);
        openFiles.put(name, v);
      } else {
         openFiles.put(name, Integer.valueOf(1));
      }
    }
    return new MockRAMInputStream(this, name, file);
  }
  public synchronized final long getRecomputedSizeInBytes() {
    long size = 0;
    for(final RAMFile file: fileMap.values()) {
      size += file.getSizeInBytes();
    }
    return size;
  }
  public final synchronized long getRecomputedActualSizeInBytes() {
    long size = 0;
    for (final RAMFile file : fileMap.values())
      size += file.length;
    return size;
  }
  @Override
  public synchronized void close() {
    if (openFiles == null) {
      openFiles = new HashMap<String,Integer>();
    }
    if (noDeleteOpenFile && openFiles.size() > 0) {
      throw new RuntimeException("MockRAMDirectory: cannot close: there are still open files: " + openFiles);
    }
  }
  public static class Failure {
    public void eval(MockRAMDirectory dir) throws IOException { }
    public Failure reset() { return this; }
    protected boolean doFail;
    public void setDoFail() {
      doFail = true;
    }
    public void clearDoFail() {
      doFail = false;
    }
  }
  ArrayList<Failure> failures;
  synchronized public void failOn(Failure fail) {
    if (failures == null) {
      failures = new ArrayList<Failure>();
    }
    failures.add(fail);
  }
  synchronized void maybeThrowDeterministicException() throws IOException {
    if (failures != null) {
      for(int i = 0; i < failures.size(); i++) {
        failures.get(i).eval(this);
      }
    }
  }
}
