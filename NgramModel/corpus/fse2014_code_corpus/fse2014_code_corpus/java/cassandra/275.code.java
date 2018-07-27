package org.apache.cassandra.io.sstable;
import java.io.File;
import java.util.EnumSet;
import com.google.common.base.Objects;
import org.apache.cassandra.utils.Pair;
public class Component
{
    final static EnumSet<Type> TYPES = EnumSet.allOf(Type.class);
    enum Type
    {
        DATA("Data.db"),
        PRIMARY_INDEX("Index.db"),
        FILTER("Filter.db"),
        COMPACTED_MARKER("Compacted"),
        STATS("Statistics.db"),
        BITMAP_INDEX("Bitidx.db");
        final String repr;
        Type(String repr)
        {
            this.repr = repr;
        }
        static Type fromRepresentation(String repr)
        {
            for (Type type : TYPES)
                if (repr.equals(type.repr))
                    return type;
            throw new RuntimeException("Invalid SSTable component: '" + repr + "'");
        }
    }
    public final static Component DATA = new Component(Type.DATA, -1);
    public final static Component PRIMARY_INDEX = new Component(Type.PRIMARY_INDEX, -1);
    public final static Component FILTER = new Component(Type.FILTER, -1);
    public final static Component COMPACTED_MARKER = new Component(Type.COMPACTED_MARKER, -1);
    public final static Component STATS = new Component(Type.STATS, -1);
    public final Type type;
    public final int id;
    public final int hashCode;
    public Component(Type type)
    {
        this(type, -1);
    }
    public Component(Type type, int id)
    {
        this.type = type;
        this.id = id;
        this.hashCode = Objects.hashCode(type, id);
    }
    public String name()
    {
        switch(type)
        {
            case DATA:
            case PRIMARY_INDEX:
            case FILTER:
            case COMPACTED_MARKER:
            case STATS:
                return type.repr;
            case BITMAP_INDEX:
                return String.format("%d-%s", id, type.repr);
        }
        throw new IllegalStateException();
    }
    public static Pair<Descriptor,Component> fromFilename(File directory, String name)
    {
        Pair<Descriptor,String> path = Descriptor.fromFilename(directory, name);
        String repr = path.right;
        int id = -1;
        int separatorPos = repr.indexOf('-');
        if (separatorPos != -1)
        {
            id = Integer.parseInt(repr.substring(0, separatorPos));
            repr = repr.substring(separatorPos+1, repr.length());
        }
        Type type = Type.fromRepresentation(repr);
        Component component;
        switch(type)
        {
            case DATA:              component = Component.DATA;             break;
            case PRIMARY_INDEX:     component = Component.PRIMARY_INDEX;    break;
            case FILTER:            component = Component.FILTER;           break;
            case COMPACTED_MARKER:  component = Component.COMPACTED_MARKER; break;
            case STATS:             component = Component.STATS;            break;
            case BITMAP_INDEX:
                 component = new Component(type, id);
                 break;
            default:
                 throw new IllegalStateException();
        }
        return new Pair<Descriptor,Component>(path.left, component);
    }
    @Override
    public String toString()
    {
        return this.name();
    }
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof Component))
            return false;
        Component that = (Component)o;
        return this.type == that.type && this.id == that.id;
    }
    @Override
    public int hashCode()
    {
        return hashCode;
    }
}
