package org.apache.batik.anim;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.dom.anim.AnimatableElement;
import org.apache.batik.ext.awt.geom.Cubic;
import org.apache.batik.util.SMILConstants;
public abstract class InterpolatingAnimation extends AbstractAnimation {
    protected int calcMode;
    protected float[] keyTimes;
    protected float[] keySplines;
    protected Cubic[] keySplineCubics;
    protected boolean additive;
    protected boolean cumulative;
    public InterpolatingAnimation(TimedElement timedElement,
                                  AnimatableElement animatableElement,
                                  int calcMode,
                                  float[] keyTimes,
                                  float[] keySplines,
                                  boolean additive,
                                  boolean cumulative) {
        super(timedElement, animatableElement);
        this.calcMode = calcMode;
        this.keyTimes = keyTimes;
        this.keySplines = keySplines;
        this.additive = additive;
        this.cumulative = cumulative;
        if (calcMode == CALC_MODE_SPLINE) {
            if (keySplines == null || keySplines.length % 4 != 0) {
                throw timedElement.createException
                    ("attribute.malformed",
                     new Object[] { null,
                                    SMILConstants.SMIL_KEY_SPLINES_ATTRIBUTE });
            }
            keySplineCubics = new Cubic[keySplines.length / 4];
            for (int i = 0; i < keySplines.length / 4; i++) {
                keySplineCubics[i] = new Cubic(0, 0,
                                               keySplines[i * 4],
                                               keySplines[i * 4 + 1],
                                               keySplines[i * 4 + 2],
                                               keySplines[i * 4 + 3],
                                               1, 1);
            }
        }
        if (keyTimes != null) {
            boolean invalidKeyTimes = false;
            if ((calcMode == CALC_MODE_LINEAR || calcMode == CALC_MODE_SPLINE
                        || calcMode == CALC_MODE_PACED)
                    && (keyTimes.length < 2
                        || keyTimes[0] != 0
                        || keyTimes[keyTimes.length - 1] != 1)
                    || calcMode == CALC_MODE_DISCRETE
                        && (keyTimes.length == 0 || keyTimes[0] != 0)) {
                invalidKeyTimes = true;
            }
            if (!invalidKeyTimes) {
                for (int i = 1; i < keyTimes.length; i++) {
                    if (keyTimes[i] < 0 || keyTimes[1] > 1
                            || keyTimes[i] < keyTimes[i - 1]) {
                        invalidKeyTimes = true;
                        break;
                    }
                }
            }
            if (invalidKeyTimes) {
                throw timedElement.createException
                    ("attribute.malformed",
                     new Object[] { null,
                                    SMILConstants.SMIL_KEY_TIMES_ATTRIBUTE });
            }
        }
    }
    protected boolean willReplace() {
        return !additive;
    }
    protected void sampledLastValue(int repeatIteration) {
        sampledAtUnitTime(1f, repeatIteration);
    }
    protected void sampledAt(float simpleTime, float simpleDur,
                             int repeatIteration) {
        float unitTime;
        if (simpleDur == TimedElement.INDEFINITE) {
            unitTime = 0;
        } else {
            unitTime = simpleTime / simpleDur;
        }
        sampledAtUnitTime(unitTime, repeatIteration);
    }
    protected abstract void sampledAtUnitTime(float unitTime,
                                              int repeatIteration);
}
