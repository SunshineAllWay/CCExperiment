package refineunit;

import java.util.ArrayList;
import model.BasicNGram;

/**
 * @author HHeart
 * Interpolation Smoothing
 */

public class Interpolation {
	public int submodelNum;
	public ArrayList<BasicNGram<?>> ngramModelList;
	private ArrayList<Double> lambda;
	
	public Interpolation(int num, ArrayList<BasicNGram<?>> ngramList) {
		submodelNum = num;
		ngramModelList= ngramList;
		lambda = new ArrayList<>();
	}
	
	public void estimateParameter () {
		//TODO
	}
	
	public ArrayList<Double> getLambdaList() {
		return lambda;
	}
}