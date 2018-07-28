package org.apache.cassandra.gms;
import java.util.BitSet;
import java.util.Random;
class PureRandom extends Random
{
    private BitSet bs_ = new BitSet();
    private int lastUb_;
    PureRandom()
    {
        super();
    }
    public int nextInt(int ub)
    {
    	if (ub <= 0)
    		throw new IllegalArgumentException("ub must be positive");
        if ( lastUb_ !=  ub )
        {
            bs_.clear();
            lastUb_ = ub;
        }
        else if(bs_.cardinality() == ub)
        {
        	bs_.clear();
        }
        int value = super.nextInt(ub);
        while ( bs_.get(value) )
        {
            value = super.nextInt(ub);
        }
        bs_.set(value);
        return value;
    }
    public static void main(String[] args) throws Throwable
    {
    	Random pr = new PureRandom();
        int ubs[] = new int[] { 2, 3, 1, 10, 5, 0};
        for (int ub : ubs)
        {
            System.out.println("UB: " + String.valueOf(ub));
            for (int j = 0; j < 10; j++)
            {
                int junk = pr.nextInt(ub);
                System.out.println(junk);
            }
        }
    }
}
