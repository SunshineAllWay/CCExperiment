package org.apache.lucene.util;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.analysis.TokenStream; 
public class AttributeSource {
  public static abstract class AttributeFactory {
    public abstract AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass);
    public static final AttributeFactory DEFAULT_ATTRIBUTE_FACTORY = new DefaultAttributeFactory();
    private static final class DefaultAttributeFactory extends AttributeFactory {
      private static final WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends AttributeImpl>>> attClassImplMap =
        new WeakHashMap<Class<? extends Attribute>, WeakReference<Class<? extends AttributeImpl>>>();
      private DefaultAttributeFactory() {}
      @Override
      public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
        try {
          return getClassForInterface(attClass).newInstance();
        } catch (InstantiationException e) {
          throw new IllegalArgumentException("Could not instantiate implementing class for " + attClass.getName());
        } catch (IllegalAccessException e) {
          throw new IllegalArgumentException("Could not instantiate implementing class for " + attClass.getName());
        }
      }
      private static Class<? extends AttributeImpl> getClassForInterface(Class<? extends Attribute> attClass) {
        synchronized(attClassImplMap) {
          final WeakReference<Class<? extends AttributeImpl>> ref = attClassImplMap.get(attClass);
          Class<? extends AttributeImpl> clazz = (ref == null) ? null : ref.get();
          if (clazz == null) {
            try {
              attClassImplMap.put(attClass,
                new WeakReference<Class<? extends AttributeImpl>>(
                  clazz = Class.forName(attClass.getName() + "Impl", true, attClass.getClassLoader())
                  .asSubclass(AttributeImpl.class)
                )
              );
            } catch (ClassNotFoundException e) {
              throw new IllegalArgumentException("Could not find implementing class for " + attClass.getName());
            }
          }
          return clazz;
        }
      }
    }
  }
  private final Map<Class<? extends Attribute>, AttributeImpl> attributes;
  private final Map<Class<? extends AttributeImpl>, AttributeImpl> attributeImpls;
  private AttributeFactory factory;
  public AttributeSource() {
    this(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
  }
  public AttributeSource(AttributeSource input) {
    if (input == null) {
      throw new IllegalArgumentException("input AttributeSource must not be null");
    }
    this.attributes = input.attributes;
    this.attributeImpls = input.attributeImpls;
    this.factory = input.factory;
  }
  public AttributeSource(AttributeFactory factory) {
    this.attributes = new LinkedHashMap<Class<? extends Attribute>, AttributeImpl>();
    this.attributeImpls = new LinkedHashMap<Class<? extends AttributeImpl>, AttributeImpl>();
    this.factory = factory;
  }
  public AttributeFactory getAttributeFactory() {
    return this.factory;
  }
  public Iterator<Class<? extends Attribute>> getAttributeClassesIterator() {
    return Collections.unmodifiableSet(attributes.keySet()).iterator();
  }
  public Iterator<AttributeImpl> getAttributeImplsIterator() {
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      final State initState = currentState;
      return new Iterator<AttributeImpl>() {
        private State state = initState;
        public void remove() {
          throw new UnsupportedOperationException();
        }
        public AttributeImpl next() {
          if (state == null)
            throw new NoSuchElementException();
          final AttributeImpl att = state.attribute;
          state = state.next;
          return att;
        }
        public boolean hasNext() {
          return state != null;
        }
      };
    } else {
      return Collections.<AttributeImpl>emptySet().iterator();
    }
  }
  private static final WeakHashMap<Class<? extends AttributeImpl>,LinkedList<WeakReference<Class<? extends Attribute>>>> knownImplClasses =
    new WeakHashMap<Class<? extends AttributeImpl>,LinkedList<WeakReference<Class<? extends Attribute>>>>();
  public void addAttributeImpl(final AttributeImpl att) {
    final Class<? extends AttributeImpl> clazz = att.getClass();
    if (attributeImpls.containsKey(clazz)) return;
    LinkedList<WeakReference<Class<? extends Attribute>>> foundInterfaces;
    synchronized(knownImplClasses) {
      foundInterfaces = knownImplClasses.get(clazz);
      if (foundInterfaces == null) {
        knownImplClasses.put(clazz, foundInterfaces = new LinkedList<WeakReference<Class<? extends Attribute>>>());
        Class<?> actClazz = clazz;
        do {
          for (Class<?> curInterface : actClazz.getInterfaces()) {
            if (curInterface != Attribute.class && Attribute.class.isAssignableFrom(curInterface)) {
              foundInterfaces.add(new WeakReference<Class<? extends Attribute>>(curInterface.asSubclass(Attribute.class)));
            }
          }
          actClazz = actClazz.getSuperclass();
        } while (actClazz != null);
      }
    }
    for (WeakReference<Class<? extends Attribute>> curInterfaceRef : foundInterfaces) {
      final Class<? extends Attribute> curInterface = curInterfaceRef.get();
      assert (curInterface != null) :
        "We have a strong reference on the class holding the interfaces, so they should never get evicted";
      if (!attributes.containsKey(curInterface)) {
        this.currentState = null;
        attributes.put(curInterface, att);
        attributeImpls.put(clazz, att);
      }
    }
  }
  public <A extends Attribute> A addAttribute(Class<A> attClass) {
    AttributeImpl attImpl = attributes.get(attClass);
    if (attImpl == null) {
      if (!(attClass.isInterface() && Attribute.class.isAssignableFrom(attClass))) {
        throw new IllegalArgumentException(
          "addAttribute() only accepts an interface that extends Attribute, but " +
          attClass.getName() + " does not fulfil this contract."
        );
      }
      addAttributeImpl(attImpl = this.factory.createAttributeInstance(attClass));
    }
    return attClass.cast(attImpl);
  }
  public boolean hasAttributes() {
    return !this.attributes.isEmpty();
  }
  public boolean hasAttribute(Class<? extends Attribute> attClass) {
    return this.attributes.containsKey(attClass);
  }
  public <A extends Attribute> A getAttribute(Class<A> attClass) {
    AttributeImpl attImpl = attributes.get(attClass);
    if (attImpl == null) {
      throw new IllegalArgumentException("This AttributeSource does not have the attribute '" + attClass.getName() + "'.");
    }
    return attClass.cast(attImpl);
  }
  public static final class State implements Cloneable {
    AttributeImpl attribute;
    State next;
    @Override
    public Object clone() {
      State clone = new State();
      clone.attribute = (AttributeImpl) attribute.clone();
      if (next != null) {
        clone.next = (State) next.clone();
      }
      return clone;
    }
  }
  private State currentState = null;
  private void computeCurrentState() {
    currentState = new State();
    State c = currentState;
    final Iterator<AttributeImpl> it = attributeImpls.values().iterator();
    c.attribute = it.next();
    while (it.hasNext()) {
      c.next = new State();
      c = c.next;
      c.attribute = it.next();
    }        
  }
  public void clearAttributes() {
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      for (State state = currentState; state != null; state = state.next) {
        state.attribute.clear();
      }
    }
  }
  public State captureState() {
    if (!hasAttributes()) {
      return null;
    }
    if (currentState == null) {
      computeCurrentState();
    }
    return (State) this.currentState.clone();
  }
  public void restoreState(State state) {
    if (state == null)  return;
    do {
      AttributeImpl targetImpl = attributeImpls.get(state.attribute.getClass());
      if (targetImpl == null) {
        throw new IllegalArgumentException("State contains AttributeImpl of type " +
          state.attribute.getClass().getName() + " that is not in in this AttributeSource");
      }
      state.attribute.copyTo(targetImpl);
      state = state.next;
    } while (state != null);
  }
  @Override
  public int hashCode() {
    int code = 0;
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      for (State state = currentState; state != null; state = state.next) {
        code = code * 31 + state.attribute.hashCode();
      }
    }
    return code;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AttributeSource) {
      AttributeSource other = (AttributeSource) obj;  
      if (hasAttributes()) {
        if (!other.hasAttributes()) {
          return false;
        }
        if (this.attributeImpls.size() != other.attributeImpls.size()) {
          return false;
        }
        if (this.currentState == null) {
          this.computeCurrentState();
        }
        State thisState = this.currentState;
        if (other.currentState == null) {
          other.computeCurrentState();
        }
        State otherState = other.currentState;
        while (thisState != null && otherState != null) {
          if (otherState.attribute.getClass() != thisState.attribute.getClass() || !otherState.attribute.equals(thisState.attribute)) {
            return false;
          }
          thisState = thisState.next;
          otherState = otherState.next;
        }
        return true;
      } else {
        return !other.hasAttributes();
      }
    } else
      return false;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append('(');
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      for (State state = currentState; state != null; state = state.next) {
        if (state != currentState) sb.append(',');
        sb.append(state.attribute.toString());
      }
    }
    return sb.append(')').toString();
  }
  public AttributeSource cloneAttributes() {
    final AttributeSource clone = new AttributeSource(this.factory);
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      for (State state = currentState; state != null; state = state.next) {
        clone.attributeImpls.put(state.attribute.getClass(), (AttributeImpl) state.attribute.clone());
      }
      for (Entry<Class<? extends Attribute>, AttributeImpl> entry : this.attributes.entrySet()) {
        clone.attributes.put(entry.getKey(), clone.attributeImpls.get(entry.getValue().getClass()));
      }
    }
    return clone;
  }
  public final void copyTo(AttributeSource target) {
    if (hasAttributes()) {
      if (currentState == null) {
        computeCurrentState();
      }
      for (State state = currentState; state != null; state = state.next) {
        final AttributeImpl targetImpl = target.attributeImpls.get(state.attribute.getClass());
        if (targetImpl == null) {
          throw new IllegalArgumentException("This AttributeSource contains AttributeImpl of type " +
            state.attribute.getClass().getName() + " that is not in the target");
        }
        state.attribute.copyTo(targetImpl);
      }
    }
  }
}
