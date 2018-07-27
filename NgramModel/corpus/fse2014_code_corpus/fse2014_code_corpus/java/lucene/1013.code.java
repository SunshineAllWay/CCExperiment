package org.apache.lucene.search.highlight;
public class WeightedTerm
{
	float weight; 
	String term; 
	public WeightedTerm (float weight,String term)
	{
		this.weight=weight;
		this.term=term;
	}
	public String getTerm()
	{
		return term;
	}
	public float getWeight()
	{
		return weight;
	}
	public void setTerm(String term)
	{
		this.term = term;
	}
	public void setWeight(float weight)
	{
		this.weight = weight;
	}
}
