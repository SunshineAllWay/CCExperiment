package org.apache.tools.ant.taskdefs.optional.sound;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
public class AntSoundPlayer implements LineListener, BuildListener {
    private File fileSuccess = null;
    private int loopsSuccess = 0;
    private Long durationSuccess = null;
    private File fileFail = null;
    private int loopsFail = 0;
    private Long durationFail = null;
    public AntSoundPlayer() {
    }
    public void addBuildSuccessfulSound(File file, int loops, Long duration) {
        this.fileSuccess = file;
        this.loopsSuccess = loops;
        this.durationSuccess = duration;
    }
    public void addBuildFailedSound(File fileFail, int loopsFail, Long durationFail) {
        this.fileFail = fileFail;
        this.loopsFail = loopsFail;
        this.durationFail = durationFail;
    }
    private void play(Project project, File file, int loops, Long duration) {
        Clip audioClip = null;
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException uafe) {
            project.log("Audio format is not yet supported: "
                + uafe.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (audioInputStream != null) {
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info   info = new DataLine.Info(Clip.class, format,
                                             AudioSystem.NOT_SPECIFIED);
            try {
                audioClip = (Clip) AudioSystem.getLine(info);
                audioClip.addLineListener(this);
                audioClip.open(audioInputStream);
            } catch (LineUnavailableException e) {
                project.log("The sound device is currently unavailable");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (duration != null) {
                playClip(audioClip, duration.longValue());
            } else {
                playClip(audioClip, loops);
            }
            audioClip.drain();
            audioClip.close();
        } else {
            project.log("Can't get data from file " + file.getName());
        }
    }
    private void playClip(Clip clip, int loops) {
        clip.loop(loops);
        do {
            try {
                long timeLeft =
                    (clip.getMicrosecondLength() - clip.getMicrosecondPosition())
                    / 1000;
                if (timeLeft > 0) {
                    Thread.sleep(timeLeft);
                }
            } catch (InterruptedException e) {
                break;
            }
        } while (clip.isRunning());
        if (clip.isRunning()) {
            clip.stop();
        }
    }
    private void playClip(Clip clip, long duration) {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
        clip.stop();
    }
    public void update(LineEvent event) {
        if (event.getType().equals(LineEvent.Type.STOP)) {
            Line line = event.getLine();
            line.close();
        }
    }
    public void buildStarted(BuildEvent event) {
    }
    public void buildFinished(BuildEvent event) {
        if (event.getException() == null && fileSuccess != null) {
            play(event.getProject(), fileSuccess, loopsSuccess, durationSuccess);
        } else if (event.getException() != null && fileFail != null) {
            play(event.getProject(), fileFail, loopsFail, durationFail);
        }
    }
    public void targetStarted(BuildEvent event) {
    }
    public void targetFinished(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
    }
}
