package org.apache.xml.utils;
public class StringToStringTable
{
  private int m_blocksize;
  private String m_map[];
  private int m_firstFree = 0;
  private int m_mapSize;
  public StringToStringTable()
  {
    m_blocksize = 16;
    m_mapSize = m_blocksize;
    m_map = new String[m_blocksize];
  }
  public StringToStringTable(int blocksize)
  {
    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new String[blocksize];
  }
  public final int getLength()
  {
    return m_firstFree;
  }
  public final void put(String key, String value)
  {
    if ((m_firstFree + 2) >= m_mapSize)
    {
      m_mapSize += m_blocksize;
      String newMap[] = new String[m_mapSize];
      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);
      m_map = newMap;
    }
    m_map[m_firstFree] = key;
    m_firstFree++;
    m_map[m_firstFree] = value;
    m_firstFree++;
  }
  public final String get(String key)
  {
    for (int i = 0; i < m_firstFree; i += 2)
    {
      if (m_map[i].equals(key))
        return m_map[i + 1];
    }
    return null;
  }
  public final void remove(String key)
  {
    for (int i = 0; i < m_firstFree; i += 2)
    {
      if (m_map[i].equals(key))
      {
        if ((i + 2) < m_firstFree)
          System.arraycopy(m_map, i + 2, m_map, i, m_firstFree - (i + 2));
        m_firstFree -= 2;
        m_map[m_firstFree] = null;
        m_map[m_firstFree + 1] = null;
        break;
      }
    }
  }
  public final String getIgnoreCase(String key)
  {
    if (null == key)
      return null;
    for (int i = 0; i < m_firstFree; i += 2)
    {
      if (m_map[i].equalsIgnoreCase(key))
        return m_map[i + 1];
    }
    return null;
  }
  public final String getByValue(String val)
  {
    for (int i = 1; i < m_firstFree; i += 2)
    {
      if (m_map[i].equals(val))
        return m_map[i - 1];
    }
    return null;
  }
  public final String elementAt(int i)
  {
    return m_map[i];
  }
  public final boolean contains(String key)
  {
    for (int i = 0; i < m_firstFree; i += 2)
    {
      if (m_map[i].equals(key))
        return true;
    }
    return false;
  }
  public final boolean containsValue(String val)
  {
    for (int i = 1; i < m_firstFree; i += 2)
    {
      if (m_map[i].equals(val))
        return true;
    }
    return false;
  }
}
