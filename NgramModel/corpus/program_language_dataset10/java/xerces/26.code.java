package socket;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import socket.io.WrappedInputStream;
import socket.io.WrappedOutputStream;
public class KeepSocketOpen {
    public static void main(String[] argv) throws Exception {
        final int port = 6789;
        if (argv.length == 0) {
            System.out.println("usage: java socket.KeepSocketOpen file(s)");
            System.exit(1);
        }
        Server server = new Server(port, argv);
        Client client = new Client("localhost", port);
        new Thread(server).start();
        new Thread(client).start();
    } 
    public static final class Server 
        extends ServerSocket 
        implements Runnable {
        private String[] fFilenames;
        private boolean fVerbose;
        private byte[] fBuffer;
        public Server(int port, String[] filenames) throws IOException {
            this(port, filenames, false);
        }
        public Server(int port, String[] filenames, boolean verbose) 
            throws IOException {
            super(port);
            System.out.println("Server: Created.");
            fFilenames = filenames;
            fVerbose = verbose;
            fBuffer = new byte[4096<<2];
        } 
        public void run() {
            System.out.println("Server: Running.");
            final Random random = new Random(System.currentTimeMillis());
            try {
                if (fVerbose) System.out.println("Server: Waiting for Client connection...");
                final Socket clientSocket = accept();
                final OutputStream clientStream = clientSocket.getOutputStream();
                System.out.println("Server: Client connected.");
                for (int i = 0; i < fFilenames.length; i++) {
                    String filename = fFilenames[i];
                    System.out.println("Server: Opening file \""+filename+'"');
                    FileInputStream fileIn = new FileInputStream(filename);
                    if (fVerbose) System.out.println("Server: Wrapping output stream.");
                    WrappedOutputStream wrappedOut = new WrappedOutputStream(clientStream);
                    int total = 0;
                    while (true) {
                        int length = fBuffer.length;
                        if (fVerbose) System.out.println("Server: Attempting to read "+length+" byte(s).");
                        int count = fileIn.read(fBuffer, 0, length);
                        if (count == -1) {
                            if (fVerbose) System.out.println("Server: EOF.");
                            break;
                        }
                        if (fVerbose) System.out.println("Server: Writing "+count+" byte(s) to wrapped output stream.");
                        wrappedOut.write(fBuffer, 0, count);
                        total += count;
                    }
                    System.out.println("Server: Wrote "+total+" byte(s) total.");
                    if (fVerbose) System.out.println("Server: Closing output stream.");
                    wrappedOut.close();
                    if (fVerbose) System.out.println("Server: Closing file.");
                    fileIn.close();
                }
                if (fVerbose) System.out.println("Server: Closing socket.");
                clientSocket.close();
            }
            catch (IOException e) {
                System.out.println("Server ERROR: "+e.getMessage());
            }
            System.out.println("Server: Exiting.");
        } 
    } 
    public static final class Client
        extends HandlerBase
        implements Runnable {
        private Socket fServerSocket;
        private WrappedInputStream fWrappedInputStream;
        private boolean fVerbose;
        private byte[] fBuffer;
        private SAXParser fParser;
        private int fElementCount;
        private int fAttributeCount;
        private int fIgnorableWhitespaceCount;
        private int fCharactersCount;
        private long fTimeBefore;
        public Client(String address, int port) throws IOException {
            this(address, port, false);
            fParser = new SAXParser();
            fParser.setDocumentHandler(this);
            fParser.setErrorHandler(this);
        }
        public Client(String address, int port, boolean verbose) 
            throws IOException {
            System.out.println("Client: Created.");
            fServerSocket = new Socket(address, port);
            fVerbose = verbose;
            fBuffer = new byte[1024];
        } 
        public void run() {
            System.out.println("Client: Running.");
            try {
                final InputStream serverStream = fServerSocket.getInputStream();
                while (!Thread.interrupted()) {
                    if (fVerbose) System.out.println("Client: Wrapping input stream.");
                    fWrappedInputStream = new WrappedInputStream(serverStream);
                    InputStream in = new InputStreamReporter(fWrappedInputStream);
                    if (fVerbose) System.out.println("Client: Parsing XML document.");
                    InputSource source = new InputSource(in);
                    fParser.parse(source);
                    fWrappedInputStream = null;
                    if (fVerbose) System.out.println("Client: Closing input stream.");
                    in.close();
                }
                if (fVerbose) System.out.println("Client: Closing socket.");
                fServerSocket.close();
            }
            catch (EOFException e) {
            }
            catch (Exception e) {
                System.out.println("Client ERROR: "+e.getMessage());
            }
            System.out.println("Client: Exiting.");
        } 
        public void startDocument() {
            fElementCount = 0;
            fAttributeCount = 0;
            fIgnorableWhitespaceCount = 0;
            fCharactersCount = 0;
            fTimeBefore = System.currentTimeMillis();
        } 
        public void startElement(String name, AttributeList attrs) {
            fElementCount++;
            fAttributeCount += attrs != null ? attrs.getLength() : 0;
        } 
        public void ignorableWhitespace(char[] ch, int offset, int length) {
            fIgnorableWhitespaceCount += length;
        } 
        public void characters(char[] ch, int offset, int length) {
            fCharactersCount += length;
        } 
        public void endDocument() {
            long timeAfter = System.currentTimeMillis();
            System.out.print("Client: ");
            System.out.print(timeAfter - fTimeBefore);
            System.out.print(" ms (");
            System.out.print(fElementCount);
            System.out.print(" elems, ");
            System.out.print(fAttributeCount);
            System.out.print(" attrs, ");
            System.out.print(fIgnorableWhitespaceCount);
            System.out.print(" spaces, ");
            System.out.print(fCharactersCount);
            System.out.print(" chars)");
            System.out.println();
        } 
        public void warning(SAXParseException e) throws SAXException {
            System.out.println("Client: [warning] "+e.getMessage());
        } 
        public void error(SAXParseException e) throws SAXException {
            System.out.println("Client: [error] "+e.getMessage());
        } 
        public void fatalError(SAXParseException e) throws SAXException {
            System.out.println("Client: [fatal error] "+e.getMessage());
            try {
                fWrappedInputStream.close();
            }
            catch (IOException ioe) {
            }
            throw e;
        } 
        class InputStreamReporter
            extends FilterInputStream {
            private long fTotal;
            public InputStreamReporter(InputStream stream) {
                super(stream);
            } 
            public int read() throws IOException {
                int b = super.in.read();
                if (b == -1) {
                    System.out.println("Client: Read "+fTotal+" byte(s) total.");
                    return -1;
                }
                fTotal++;
                return b;
            } 
            public int read(byte[] b, int offset, int length) 
                throws IOException {
                int count = super.in.read(b, offset, length);
                if (count == -1) {
                    System.out.println("Client: Read "+fTotal+" byte(s) total.");
                    return -1;
                }
                fTotal += count;
                if (Client.this.fVerbose) System.out.println("Client: Actually read "+count+" byte(s).");
                return count;
            } 
        } 
    } 
} 
