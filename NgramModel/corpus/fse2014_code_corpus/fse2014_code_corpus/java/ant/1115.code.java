package org.apache.tools.ant.types.mappers;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
public class MapperResult extends Task {
    private String failMessage = "";
    private String input;
    private String output;
    private FileNameMapper fileNameMapper;
    private static final String NULL_MAPPER_RESULT = "<NULL>";
    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }
    public void setInput(String input) {
        this.input = input;
    }
    public void setOutput(String output) {
        this.output = output;
    }
    public void addConfiguredMapper(Mapper mapper) {
        add(mapper.getImplementation());
    }
    public void add(FileNameMapper fileNameMapper) {
        if (this.fileNameMapper != null) {
            throw new BuildException("Only one mapper type nested element allowed");
        }
        this.fileNameMapper = fileNameMapper;
    }
    public void execute() {
        if (input == null) {
            throw new BuildException("Missing attribute 'input'");
        }
        if (output == null) {
            throw new BuildException("Missing attribute 'output'");
        }
        if (fileNameMapper == null) {
            throw new BuildException("Missing a nested file name mapper type element");
        }
        String[] result = fileNameMapper.mapFileName(input);
        String flattened;
        if (result == null) {
            flattened = NULL_MAPPER_RESULT;
        } else {
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < result.length; ++i) {
                if (i != 0) {
                    b.append("|");
                }
                b.append(result[i]);
            }
            flattened = b.toString();
        }
        if (!flattened.equals(output)) {
            throw new BuildException(
                failMessage
                + " "
                + "got "
                + flattened
                + " "
                + "expected "
                + output);
        }
    }
}
