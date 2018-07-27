package org.apache.tools.ant.input;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import org.apache.tools.ant.BuildException;
public class DefaultInputHandler implements InputHandler {
    public DefaultInputHandler() {
    }
    public void handleInput(InputRequest request) throws BuildException {
        String prompt = getPrompt(request);
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(getInputStream()));
            do {
                System.err.println(prompt);
                System.err.flush();
                try {
                    String input = r.readLine();
                    request.setInput(input);
                } catch (IOException e) {
                    throw new BuildException("Failed to read input from"
                                             + " Console.", e);
                }
            } while (!request.isInputValid());
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    throw new BuildException("Failed to close input.", e);
                }
            }
        }
    }
    protected String getPrompt(InputRequest request) {
        String prompt = request.getPrompt();
        String def = request.getDefaultValue();
        if (request instanceof MultipleChoiceInputRequest) {
            StringBuffer sb = new StringBuffer(prompt);
            sb.append(" (");
            Enumeration e =
                ((MultipleChoiceInputRequest) request).getChoices().elements();
            boolean first = true;
            while (e.hasMoreElements()) {
                if (!first) {
                    sb.append(", ");
                }
                String next = (String) e.nextElement();
                if (next.equals(def)) {
                    sb.append('[');
                }
                sb.append(next);
                if (next.equals(def)) {
                    sb.append(']');
                }
                first = false;
            }
            sb.append(")");
            return sb.toString();
        } else if (def != null) {
            return prompt + " [" + def + "]";
        } else {
            return prompt;
        }
    }
    protected InputStream getInputStream() {
        return System.in;
    }
}