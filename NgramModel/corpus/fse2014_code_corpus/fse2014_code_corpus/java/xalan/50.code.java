package org.apache.xalan.extensions;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.trace.ExtensionEvent;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
public class ExtensionHandlerJavaClass extends ExtensionHandlerJava
{
  private Class m_classObj = null;
  private Object m_defaultInstance = null;
  public ExtensionHandlerJavaClass(String namespaceUri,
                                   String scriptLang,
                                   String className)
  {
    super(namespaceUri, scriptLang, className);
    try
    {
      m_classObj = getClassForName(className);
    }
    catch (ClassNotFoundException e)
    {
    }
  }
  public boolean isFunctionAvailable(String function) 
  {
    Method[] methods = m_classObj.getMethods();
    int nMethods = methods.length;
    for (int i = 0; i < nMethods; i++)
    {
      if (methods[i].getName().equals(function))
        return true;
    }
    return false;
  }
  public boolean isElementAvailable(String element) 
  {
    Method[] methods = m_classObj.getMethods();
    int nMethods = methods.length;
    for (int i = 0; i < nMethods; i++)
    {
      if (methods[i].getName().equals(element))
      {
        Class[] paramTypes = methods[i].getParameterTypes();
        if ( (paramTypes.length == 2)
          && paramTypes[0].isAssignableFrom(
                        org.apache.xalan.extensions.XSLProcessorContext.class)
          && paramTypes[1].isAssignableFrom(
                        org.apache.xalan.templates.ElemExtensionCall.class) )
        {
          return true;
        }
      }
    }
    return false;
  }
  public Object callFunction (String funcName, 
                              Vector args, 
                              Object methodKey,
                              ExpressionContext exprContext)
    throws TransformerException 
  {
    Object[] methodArgs;
    Object[][] convertedArgs;
    Class[] paramTypes;
    try
    {
      TransformerImpl trans = (exprContext != null) ?
          (TransformerImpl)exprContext.getXPathContext().getOwnerObject() : null;
      if (funcName.equals("new")) {                   
        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Constructor c = null;
        if (methodKey != null)
          c = (Constructor) getFromCache(methodKey, null, methodArgs);
        if (c != null && !trans.getDebug())
        {
          try
          {
            paramTypes = c.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, 
                        paramTypes, exprContext);
            return c.newInstance(convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
          }
        }
        c = MethodResolver.getConstructor(m_classObj, 
                                          methodArgs,
                                          convertedArgs,
                                          exprContext);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, c);
        if (trans != null && trans.getDebug()) {            
            trans.getTraceManager().fireExtensionEvent(new 
                    ExtensionEvent(trans, c, convertedArgs[0]));
            Object result;
            try {            
                result = c.newInstance(convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(new 
                        ExtensionEvent(trans, c, convertedArgs[0]));
            }
            return result;
        } else
            return c.newInstance(convertedArgs[0]);
      }
      else
      {
        int resolveType;
        Object targetObject = null;
        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Method m = null;
        if (methodKey != null)
          m = (Method) getFromCache(methodKey, null, methodArgs);
        if (m != null && !trans.getDebug())
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, 
                        paramTypes, exprContext);
            if (Modifier.isStatic(m.getModifiers()))
              return m.invoke(null, convertedArgs[0]);
            else
            {
              int nTargetArgs = convertedArgs[0].length;
              if (ExpressionContext.class.isAssignableFrom(paramTypes[0]))
                nTargetArgs--;
              if (methodArgs.length <= nTargetArgs)
                return m.invoke(m_defaultInstance, convertedArgs[0]);
              else  
              {
                targetObject = methodArgs[0];
                if (targetObject instanceof XObject)
                  targetObject = ((XObject) targetObject).object();
                return m.invoke(targetObject, convertedArgs[0]);
              }
            }
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
          }
        }
        if (args.size() > 0)
        {
          targetObject = methodArgs[0];
          if (targetObject instanceof XObject)
            targetObject = ((XObject) targetObject).object();
          if (m_classObj.isAssignableFrom(targetObject.getClass()))
            resolveType = MethodResolver.DYNAMIC;
          else
            resolveType = MethodResolver.STATIC_AND_INSTANCE;
        }
        else
        {
          targetObject = null;
          resolveType = MethodResolver.STATIC_AND_INSTANCE;
        }
        m = MethodResolver.getMethod(m_classObj,
                                     funcName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     resolveType);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, m);
        if (MethodResolver.DYNAMIC == resolveType) {         
          if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, targetObject, 
                        convertedArgs[0]);
            Object result;
            try {
                result = m.invoke(targetObject, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, targetObject, 
                        convertedArgs[0]);
            }
            return result;
          } else                  
            return m.invoke(targetObject, convertedArgs[0]);
        }
        else                                  
        {
          if (Modifier.isStatic(m.getModifiers())) {
            if (trans != null && trans.getDebug()) {
              trans.getTraceManager().fireExtensionEvent(m, null, 
                        convertedArgs[0]);
              Object result;
              try {
                  result = m.invoke(null, convertedArgs[0]);
              } catch (Exception e) {
                throw e;
              } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, null, 
                        convertedArgs[0]);
              }
              return result;
            } else                  
              return m.invoke(null, convertedArgs[0]);
          }
          else
          {
            if (null == m_defaultInstance)
            {
              if (trans != null && trans.getDebug()) {
                trans.getTraceManager().fireExtensionEvent(new 
                        ExtensionEvent(trans, m_classObj));
                try {
                    m_defaultInstance = m_classObj.newInstance();
                } catch (Exception e) {
                    throw e;
                } finally {
                    trans.getTraceManager().fireExtensionEndEvent(new 
                        ExtensionEvent(trans, m_classObj));
                }
              }    else
                  m_defaultInstance = m_classObj.newInstance();
            }
            if (trans != null && trans.getDebug()) {
              trans.getTraceManager().fireExtensionEvent(m, m_defaultInstance, 
                    convertedArgs[0]);
              Object result;
              try {
                result = m.invoke(m_defaultInstance, convertedArgs[0]);
              } catch (Exception e) {
                throw e;
              } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, 
                        m_defaultInstance, convertedArgs[0]);
              }
              return result;
            } else                  
              return m.invoke(m_defaultInstance, convertedArgs[0]);
          }  
        }
      }
    }
    catch (InvocationTargetException ite)
    {
      Throwable resultException = ite;
      Throwable targetException = ite.getTargetException();
      if (targetException instanceof TransformerException)
        throw ((TransformerException)targetException);
      else if (targetException != null)
        resultException = targetException;
      throw new TransformerException(resultException);
    }
    catch (Exception e)
    {
      throw new TransformerException(e);
    }
  }
  public Object callFunction(FuncExtFunction extFunction,
                             Vector args,
                             ExpressionContext exprContext)
      throws TransformerException
  {
    return callFunction(extFunction.getFunctionName(), args, 
                        extFunction.getMethodKey(), exprContext);
  }
  public void processElement(String localPart,
                             ElemTemplateElement element,
                             TransformerImpl transformer,
                             Stylesheet stylesheetTree,
                             Object methodKey)
    throws TransformerException, IOException
  {
    Object result = null;
    Method m = (Method) getFromCache(methodKey, null, null);
    if (null == m)
    {
      try
      {
        m = MethodResolver.getElementMethod(m_classObj, localPart);
        if ( (null == m_defaultInstance) && 
                !Modifier.isStatic(m.getModifiers()) ) {
          if (transformer.getDebug()) {            
            transformer.getTraceManager().fireExtensionEvent(
                    new ExtensionEvent(transformer, m_classObj));
            try {
              m_defaultInstance = m_classObj.newInstance();
            } catch (Exception e) {
              throw e;
            } finally {
              transformer.getTraceManager().fireExtensionEndEvent(
                    new ExtensionEvent(transformer, m_classObj));
            }
          } else 
            m_defaultInstance = m_classObj.newInstance();
        }
      }
      catch (Exception e)
      {
        throw new TransformerException (e.getMessage (), e);
      }
      putToCache(methodKey, null, null, m);
    }
    XSLProcessorContext xpc = new XSLProcessorContext(transformer, 
                                                      stylesheetTree);
    try
    {
      if (transformer.getDebug()) {
        transformer.getTraceManager().fireExtensionEvent(m, m_defaultInstance, 
                new Object[] {xpc, element});
        try {
          result = m.invoke(m_defaultInstance, new Object[] {xpc, element});
        } catch (Exception e) {
          throw e;
        } finally {
          transformer.getTraceManager().fireExtensionEndEvent(m, 
                m_defaultInstance, new Object[] {xpc, element});
        }
      } else                  
        result = m.invoke(m_defaultInstance, new Object[] {xpc, element});
    }
    catch (InvocationTargetException e)
    {
      Throwable targetException = e.getTargetException();
      if (targetException instanceof TransformerException)
        throw (TransformerException)targetException;
      else if (targetException != null)
        throw new TransformerException (targetException.getMessage (), 
                targetException);
      else
        throw new TransformerException (e.getMessage (), e);
    }
    catch (Exception e)
    {
      throw new TransformerException (e.getMessage (), e);
    }
    if (result != null)
    {
      xpc.outputToResultTree (stylesheetTree, result);
    }
  }
}
