package org.apache.tools.ant.input;
import java.util.LinkedHashSet;
import java.util.Vector;
public class MultipleChoiceInputRequest extends InputRequest {
    private final LinkedHashSet choices;
    public MultipleChoiceInputRequest(String prompt, Vector choices) {
        super(prompt);
        if (choices == null) {
            throw new IllegalArgumentException("choices must not be null");
        }
        this.choices = new LinkedHashSet(choices);
    }
    public Vector getChoices() {
        return new Vector(choices);
    }
    public boolean isInputValid() {
        return choices.contains(getInput()) || ("".equals(getInput()) && getDefaultValue() != null);
    }
}
