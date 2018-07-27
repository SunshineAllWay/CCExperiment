package org.apache.log4j.chainsaw;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
class LoggingReceiver extends Thread {
    private static final Logger LOG = Logger.getLogger(LoggingReceiver.class);
    private class Slurper implements Runnable {
        private final Socket mClient;
        Slurper(Socket aClient) {
            mClient = aClient;
        }
        public void run() {
            LOG.debug("Starting to get data");
            try {
                final ObjectInputStream ois =
                    new ObjectInputStream(mClient.getInputStream());
                while (true) {
                    final LoggingEvent event = (LoggingEvent) ois.readObject();
                    mModel.addEvent(new EventDetails(event));
                }
            } catch (EOFException e) {
                LOG.info("Reached EOF, closing connection");
            } catch (SocketException e) {
                LOG.info("Caught SocketException, closing connection");
            } catch (IOException e) {
                LOG.warn("Got IOException, closing connection", e);
            } catch (ClassNotFoundException e) {
                LOG.warn("Got ClassNotFoundException, closing connection", e);
            }
            try {
                mClient.close();
            } catch (IOException e) {
                LOG.warn("Error closing connection", e);
            }
        }
    }
    private MyTableModel mModel;
    private ServerSocket mSvrSock;
    LoggingReceiver(MyTableModel aModel, int aPort) throws IOException {
        setDaemon(true);
        mModel = aModel;
        mSvrSock = new ServerSocket(aPort);
    }
    public void run() {
        LOG.info("Thread started");
        try {
            while (true) {
                LOG.debug("Waiting for a connection");
                final Socket client = mSvrSock.accept();
                LOG.debug("Got a connection from " +
                          client.getInetAddress().getHostName());
                final Thread t = new Thread(new Slurper(client));
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
            LOG.error("Error in accepting connections, stopping.", e);
        }
    }
}
