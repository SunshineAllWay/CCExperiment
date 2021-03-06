package org.apache.solr.client.solrj.impl;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
public class LBHttpSolrServer extends SolrServer {
  private final CopyOnWriteArrayList<ServerWrapper> aliveServers = new CopyOnWriteArrayList<ServerWrapper>();
  private final CopyOnWriteArrayList<ServerWrapper> zombieServers = new CopyOnWriteArrayList<ServerWrapper>();
  private ScheduledExecutorService aliveCheckExecutor;
  private HttpClient httpClient;
  private final AtomicInteger counter = new AtomicInteger(-1);
  private ReentrantLock checkLock = new ReentrantLock();
  private static final SolrQuery solrQuery = new SolrQuery("*:*");
  static {
    solrQuery.setRows(0);
  }
  private static class ServerWrapper {
    final CommonsHttpSolrServer solrServer;
    long lastUsed, lastChecked;
    int failedPings = 0;
    public ServerWrapper(CommonsHttpSolrServer solrServer) {
      this.solrServer = solrServer;
    }
    public String toString() {
      return solrServer.getBaseURL();
    }
  }
  public LBHttpSolrServer(String... solrServerUrls) throws MalformedURLException {
    this(new HttpClient(new MultiThreadedHttpConnectionManager()), solrServerUrls);
  }
  public LBHttpSolrServer(HttpClient httpClient, String... solrServerUrl)
          throws MalformedURLException {
    this(httpClient, new BinaryResponseParser(), solrServerUrl);
  }
  public LBHttpSolrServer(HttpClient httpClient, ResponseParser parser, String... solrServerUrl)
          throws MalformedURLException {
    this.httpClient = httpClient;
    for (String s : solrServerUrl) {
      aliveServers.add(new ServerWrapper(new CommonsHttpSolrServer(s, httpClient, parser)));
    }
  }
  public void addSolrServer(String server) throws MalformedURLException {
    CommonsHttpSolrServer solrServer = new CommonsHttpSolrServer(server, httpClient);
    checkLock.lock();
    try {
      aliveServers.add(new ServerWrapper(solrServer));
    } finally {
      checkLock.unlock();
    }
  }
  public String removeSolrServer(String server) {
    try {
      server = new URL(server).toExternalForm();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    if (server.endsWith("/")) {
      server = server.substring(0, server.length() - 1);
    }
    this.checkLock.lock();
    try {
      for (ServerWrapper serverWrapper : aliveServers) {
        if (serverWrapper.solrServer.getBaseURL().equals(server)) {
          aliveServers.remove(serverWrapper);
          return serverWrapper.solrServer.getBaseURL();
        }
      }
      if (zombieServers.isEmpty()) return null;
      for (ServerWrapper serverWrapper : zombieServers) {
        if (serverWrapper.solrServer.getBaseURL().equals(server)) {
          zombieServers.remove(serverWrapper);
          return serverWrapper.solrServer.getBaseURL();
        }
      }
    } finally {
      checkLock.unlock();
    }
    return null;
  }
  public void setConnectionTimeout(int timeout) {
    httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
  }
  public void setConnectionManagerTimeout(int timeout) {
    httpClient.getParams().setConnectionManagerTimeout(timeout);
  }
  public void setSoTimeout(int timeout) {
    httpClient.getParams().setSoTimeout(timeout);
  }
  public NamedList<Object> request(final SolrRequest request)
          throws SolrServerException, IOException {
    int count = counter.incrementAndGet();
    int attempts = 0;
    Exception ex;
    int startSize = aliveServers.size();
    while (true) {
      int size = aliveServers.size();
      if (size < 1) throw new SolrServerException("No live SolrServers available to handle this request");
      ServerWrapper solrServer;
      try {
        solrServer = aliveServers.get(count % size);
      } catch (IndexOutOfBoundsException e) {
        continue;
      }
      try {
        return solrServer.solrServer.request(request);
      } catch (SolrException e) {
        throw e;
      } catch (SolrServerException e) {
        if (e.getRootCause() instanceof IOException) {
          ex = e;
          moveAliveToDead(solrServer);
        } else {
          throw e;
        }
      } catch (Exception e) {
        throw new SolrServerException(e);
      }
      attempts++;
      if (attempts >= startSize)
        throw new SolrServerException("No live SolrServers available to handle this request", ex);
    }
  }
  private void checkAZombieServer(ServerWrapper zombieServer) {
    long currTime = System.currentTimeMillis();
    checkLock.lock();
    try {
      zombieServer.lastChecked = currTime;
      QueryResponse resp = zombieServer.solrServer.query(solrQuery);
      if (resp.getStatus() == 0) {
        zombieServer.lastUsed = currTime;
        zombieServers.remove(zombieServer);
        aliveServers.add(zombieServer);
        zombieServer.failedPings = 0;
      }
    } catch (Exception e) {
      zombieServer.failedPings++;
    } finally {
      checkLock.unlock();
    }
  }
  private void moveAliveToDead(ServerWrapper solrServer) {
    checkLock.lock();
    try {
      boolean result = aliveServers.remove(solrServer);
      if (result) {
        if (zombieServers.addIfAbsent(solrServer)) {
          startAliveCheckExecutor();
        }
      }
    } finally {
      checkLock.unlock();
    }
  }
  private int interval = CHECK_INTERVAL;
  public void setAliveCheckInterval(int interval) {
    if (interval <= 0) {
      throw new IllegalArgumentException("Alive check interval must be " +
              "positive, specified value = " + interval);
    }
    this.interval = interval;
  }
  private void startAliveCheckExecutor() {
    if (aliveCheckExecutor == null) {
      synchronized (this) {
        if (aliveCheckExecutor == null) {
          aliveCheckExecutor = Executors.newSingleThreadScheduledExecutor();
          aliveCheckExecutor.scheduleAtFixedRate(
                  getAliveCheckRunner(new WeakReference<LBHttpSolrServer>(this)),
                  this.interval, this.interval, TimeUnit.MILLISECONDS);
        }
      }
    }
  }
  private static Runnable getAliveCheckRunner(final WeakReference<LBHttpSolrServer> lbHttpSolrServer) {
    return new Runnable() {
      public void run() {
        LBHttpSolrServer solrServer = lbHttpSolrServer.get();
        if (solrServer != null && solrServer.zombieServers != null) {
          for (ServerWrapper zombieServer : solrServer.zombieServers) {
            solrServer.checkAZombieServer(zombieServer);
          }
        }
      }
    };
  }
  public HttpClient getHttpClient() {
    return httpClient;
  }
  protected void finalize() throws Throwable {
    try {
      if(this.aliveCheckExecutor!=null)
        this.aliveCheckExecutor.shutdownNow();
    } finally {
      super.finalize();
    }
  }
  private static final int CHECK_INTERVAL = 60 * 1000; 
}
