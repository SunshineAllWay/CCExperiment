package org.apache.batik.ext.awt.image.renderable;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.ComponentTransferFunction;
import org.apache.batik.ext.awt.image.DiscreteTransfer;
import org.apache.batik.ext.awt.image.GammaTransfer;
import org.apache.batik.ext.awt.image.IdentityTransfer;
import org.apache.batik.ext.awt.image.LinearTransfer;
import org.apache.batik.ext.awt.image.TableTransfer;
import org.apache.batik.ext.awt.image.TransferFunction;
import org.apache.batik.ext.awt.image.rendered.ComponentTransferRed;
public class ComponentTransferRable8Bit
    extends    AbstractColorInterpolationRable
    implements ComponentTransferRable {
    public static final int ALPHA = 0;
    public static final int RED   = 1;
    public static final int GREEN = 2;
    public static final int BLUE  = 3;
    private ComponentTransferFunction[]
        functions = new ComponentTransferFunction[4];
    private TransferFunction[]
        txfFunc = new TransferFunction[4];
    public ComponentTransferRable8Bit(Filter src,
                                      ComponentTransferFunction alphaFunction,
                                      ComponentTransferFunction redFunction,
                                      ComponentTransferFunction greenFunction,
                                      ComponentTransferFunction blueFunction){
        super(src, null);
        setAlphaFunction(alphaFunction);
        setRedFunction(redFunction);
        setGreenFunction(greenFunction);
        setBlueFunction(blueFunction);
    }
    public void setSource(Filter src){
        init(src, null);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public ComponentTransferFunction getAlphaFunction(){
        return functions[ALPHA];
    }
    public void setAlphaFunction(ComponentTransferFunction alphaFunction){
        touch();
        functions[ALPHA] = alphaFunction;
        txfFunc[ALPHA] = null;
    }
    public ComponentTransferFunction getRedFunction(){
        return functions[RED];
    }
    public void setRedFunction(ComponentTransferFunction redFunction){
        touch();
        functions[RED] = redFunction;
        txfFunc[RED] = null;
    }
    public ComponentTransferFunction getGreenFunction(){
        return functions[GREEN];
    }
    public void setGreenFunction(ComponentTransferFunction greenFunction){
        touch();
        functions[GREEN] = greenFunction;
        txfFunc[GREEN] = null;
    }
    public ComponentTransferFunction getBlueFunction(){
        return functions[BLUE];
    }
    public void setBlueFunction(ComponentTransferFunction blueFunction){
        touch();
        functions[BLUE] = blueFunction;
        txfFunc[BLUE] = null;
    }
    public RenderedImage createRendering(RenderContext rc){
        RenderedImage srcRI = getSource().createRendering(rc);
        if(srcRI == null)
            return null;
        return new ComponentTransferRed(convertSourceCS(srcRI),
                                        getTransferFunctions(),
                                        rc.getRenderingHints());
    }
    private TransferFunction[] getTransferFunctions(){
        TransferFunction[] txfFunc = new TransferFunction[4];
        System.arraycopy(this.txfFunc, 0, txfFunc, 0, 4);
        ComponentTransferFunction[] functions;
        functions = new ComponentTransferFunction[4];
        System.arraycopy(this.functions, 0, functions, 0, 4);
        for(int i=0; i<4; i++){
            if(txfFunc[i] == null){
                txfFunc[i] = getTransferFunction(functions[i]);
                synchronized(this.functions){
                    if(this.functions[i] == functions[i]){
                        this.txfFunc[i] = txfFunc[i];
                    }
                }
            }
        }
        return txfFunc;
    }
    private static TransferFunction getTransferFunction
        (ComponentTransferFunction function){
        TransferFunction txfFunc = null;
        if(function == null){
            txfFunc = new IdentityTransfer();
        }
        else{
            switch(function.getType()){
            case ComponentTransferFunction.IDENTITY:
                txfFunc = new IdentityTransfer();
                break;
            case ComponentTransferFunction.TABLE:
                txfFunc = new TableTransfer(tableFloatToInt(function.getTableValues()));
                break;
            case ComponentTransferFunction.DISCRETE:
                txfFunc = new DiscreteTransfer(tableFloatToInt(function.getTableValues()));
                break;
            case ComponentTransferFunction.LINEAR:
                txfFunc = new LinearTransfer(function.getSlope(),
                                             function.getIntercept());
                break;
            case ComponentTransferFunction.GAMMA:
                txfFunc = new GammaTransfer(function.getAmplitude(),
                                            function.getExponent(),
                                            function.getOffset());
                break;
            default:
                throw new Error();
            }
        }
        return txfFunc;
    }
    private static int[] tableFloatToInt(float[] tableValues){
        int[] values = new int[tableValues.length];
        for(int i=0; i<tableValues.length; i++){
            values[i] = (int)(tableValues[i]*255f);
        }
        return values;
    }
}
