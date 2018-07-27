package org.apache.lucene.misc;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
public class ChainedFilter extends Filter
{
    public static final int OR = 0;
    public static final int AND = 1;
    public static final int ANDNOT = 2;
    public static final int XOR = 3;
    public static int DEFAULT = OR;
    private Filter[] chain = null;
    private int[] logicArray;
    private int logic = -1;
    public ChainedFilter(Filter[] chain)
    {
        this.chain = chain;
    }
    public ChainedFilter(Filter[] chain, int[] logicArray)
    {
        this.chain = chain;
        this.logicArray = logicArray;
    }
    public ChainedFilter(Filter[] chain, int logic)
    {
        this.chain = chain;
        this.logic = logic;
    }
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException
    {
        int[] index = new int[1]; 
        index[0] = 0;             
        if (logic != -1)
            return getDocIdSet(reader, logic, index);
        else if (logicArray != null)
            return getDocIdSet(reader, logicArray, index);
        else
            return getDocIdSet(reader, DEFAULT, index);
    }
    private DocIdSetIterator getDISI(Filter filter, IndexReader reader)
    throws IOException {
        DocIdSet docIdSet = filter.getDocIdSet(reader);
        if (docIdSet == null) {
          return DocIdSet.EMPTY_DOCIDSET.iterator();
        } else {
          DocIdSetIterator iter = docIdSet.iterator();
          if (iter == null) {
            return DocIdSet.EMPTY_DOCIDSET.iterator();
          } else {
            return iter;
          }
        }
    }
    private OpenBitSetDISI initialResult(IndexReader reader, int logic, int[] index)
    throws IOException
    {
        OpenBitSetDISI result;
        if (logic == AND)
        {
            result = new OpenBitSetDISI(getDISI(chain[index[0]], reader), reader.maxDoc());
            ++index[0];
        }
        else if (logic == ANDNOT)
        {
            result = new OpenBitSetDISI(getDISI(chain[index[0]], reader), reader.maxDoc());
            result.flip(0,reader.maxDoc()); 
            ++index[0];
        }
        else
        {
            result = new OpenBitSetDISI(reader.maxDoc());
        }
        return result;
    }
    @Deprecated
    protected final DocIdSet finalResult(OpenBitSetDISI result, int maxDocs) {
        return result;
    }
    private DocIdSet getDocIdSet(IndexReader reader, int logic, int[] index)
    throws IOException
    {
        OpenBitSetDISI result = initialResult(reader, logic, index);
        for (; index[0] < chain.length; index[0]++)
        {
            doChain(result, logic, chain[index[0]].getDocIdSet(reader));
        }
        return finalResult(result, reader.maxDoc());
    }
    private DocIdSet getDocIdSet(IndexReader reader, int[] logic, int[] index)
    throws IOException
    {
        if (logic.length != chain.length)
            throw new IllegalArgumentException("Invalid number of elements in logic array");
        OpenBitSetDISI result = initialResult(reader, logic[0], index);
        for (; index[0] < chain.length; index[0]++)
        {
            doChain(result, logic[index[0]], chain[index[0]].getDocIdSet(reader));
        }
        return finalResult(result, reader.maxDoc());
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ChainedFilter: [");
        for (int i = 0; i < chain.length; i++)
        {
            sb.append(chain[i]);
            sb.append(' ');
        }
        sb.append(']');
        return sb.toString();
    }
    private void doChain(OpenBitSetDISI result, int logic, DocIdSet dis)
    throws IOException {
      if (dis instanceof OpenBitSet) {
        switch (logic) {
            case OR:
                result.or((OpenBitSet) dis);
                break;
            case AND:
                result.and((OpenBitSet) dis);
                break;
            case ANDNOT:
                result.andNot((OpenBitSet) dis);
                break;
            case XOR:
                result.xor((OpenBitSet) dis);
                break;
            default:
                doChain(result, DEFAULT, dis);
                break;
        }
      } else {
        DocIdSetIterator disi;
        if (dis == null) {
          disi = DocIdSet.EMPTY_DOCIDSET.iterator();
        } else {
          disi = dis.iterator();
          if (disi == null) {
            disi = DocIdSet.EMPTY_DOCIDSET.iterator();            
          }
        }
        switch (logic) {
            case OR:
                result.inPlaceOr(disi);
                break;
            case AND:
                result.inPlaceAnd(disi);
                break;
            case ANDNOT:
                result.inPlaceNot(disi);
                break;
            case XOR:
                result.inPlaceXor(disi);
                break;
            default:
                doChain(result, DEFAULT, dis);
                break;
        }
      }
    }
}
