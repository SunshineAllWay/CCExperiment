package org.apache.tools.ant;
public class BuildException extends RuntimeException {
    private static final long serialVersionUID = -5419014565354664240L;
    private Location location = Location.UNKNOWN_LOCATION;
    public BuildException() {
        super();
    }
    public BuildException(String message) {
        super(message);
    }
    public BuildException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
    public BuildException(String msg, Throwable cause, Location location) {
        this(msg, cause);
        this.location = location;
    }
    public BuildException(Throwable cause) {
        super(cause);
    }
    public BuildException(String message, Location location) {
        super(message);
        this.location = location;
    }
    public BuildException(Throwable cause, Location location) {
        this(cause);
        this.location = location;
    }
    public Throwable getException() {
        return getCause();
    }
    public String toString() {
        return location.toString() + getMessage();
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public Location getLocation() {
        return location;
    }
}
