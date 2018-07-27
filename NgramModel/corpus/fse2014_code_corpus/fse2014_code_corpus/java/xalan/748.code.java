package org.apache.xml.utils;
public class StringToStringTableVector
{
  private int m_blocksize;
  private StringToStringTable m_map[];
  private int m_firstFree = 0;
  private int m_mapSize;
  public StringToStringTableVector()
  {
    m_blocksize = 8;
    m_mapSize = m_blocksize;
    m_map = new StringToStringTable[m_blocksize];
  }
  public StringToStringTableVector(int blocksize)
  {
    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new StringToStringTable[blocksize];
  }
  public final int getLength()
  {
    return m_firstFree;
  }
  public final int size()
  {
    return m_firstFree;
  }
  public final void addElement(StringToStringTable value)
  {
    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      StringToStringTable newMap[] = new StringToStringTable[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = value;
    m_firstFree++;
  }
  public final String get(String key)
  {
    for (int i = m_firstFree - 1; i >= 0; --i)
    {
      String nsuri = m_map[i].get(key);
      if (nsuri != null)
        return nsuri;
    }
    return null;
  }
  public final boolean containsKey(String key)
  {
    for (int i = m_firstFree - 1; i >= 0; --i)
    {
      if (m_map[i].get(key) != null)
        return true;
    }
    return false;
  }
  public final void removeLastElem()
  {
    if (m_firstFree > 0)
    {
      m_map[m_firstFree] = null;
      m_firstFree--;
    }
  }
  public final StringToStringTable elementAt(int i)
  {
    return m_map[i];
  }
  public final boolean contains(StringToStringTable s)
  {
    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equals(s))
        return true;
    }
    return false;
  }
}
