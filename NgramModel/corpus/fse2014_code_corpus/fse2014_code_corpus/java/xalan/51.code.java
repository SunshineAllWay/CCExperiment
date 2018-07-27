package org.apache.xalan.extensions;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.trace.ExtensionEvent;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
public class ExtensionHandlerJavaPackage extends ExtensionHandlerJava
{
  public ExtensionHandlerJavaPackage(String namespaceUri,
                                     String scriptLang,
                                     String className)
  {
    super(namespaceUri, scriptLang, className);
  }
  public boolean isFunctionAvailable(String function) 
  {
    try
    {
      String fullName = m_className + function;
      int lastDot = fullName.lastIndexOf(".");
      if (lastDot >= 0)
      {
        Class myClass = getClassForName(fullName.substring(0, lastDot));
        Method[] methods = myClass.getMethods();
        int nMethods = methods.length;
        function = fullName.substring(lastDot + 1);
        for (int i = 0; i < nMethods; i++)
        {
          if (methods[i].getName().equals(function))
            return true;
        }
      }
    }
    catch (ClassNotFoundException cnfe) {}
    return false;
  }
  public boolean isElementAvailable(String element) 
  {
    try
    {
      String fullName = m_className + element;
      int lastDot = fullName.lastIndexOf(".");
      if (lastDot >= 0)
      {
        Class myClass = getClassForName(fullName.substring(0, lastDot));
        Method[] methods = myClass.getMethods();
        int nMethods = methods.length;
        element = fullName.substring(lastDot + 1);
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
      }
    }
    catch (ClassNotFoundException cnfe) {}
    return false;
  }
  public Object callFunction (String funcName, 
                              Vector args, 
                              Object methodKey,
                              ExpressionContext exprContext)
    throws TransformerException 
  {
    String className;
    String methodName;
    Class  classObj;
    Object targetObject;
    int lastDot = funcName.lastIndexOf(".");
    Object[] methodArgs;
    Object[][] convertedArgs;
    Class[] paramTypes;
    try
    {
      TransformerImpl trans = (exprContext != null) ?
        (TransformerImpl)exprContext.getXPathContext().getOwnerObject() : null;
      if (funcName.endsWith(".new")) {                   
        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Constructor c = (methodKey != null) ?
          (Constructor) getFromCache(methodKey, null, methodArgs) : null;
        if (c != null)
        {
          try
          {
            paramTypes = c.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
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
        className = m_className + funcName.substring(0, lastDot);
        try
        {
          classObj = getClassForName(className);
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        c = MethodResolver.getConstructor(classObj, 
                                          methodArgs,
                                          convertedArgs,
                                          exprContext);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, c);
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(new ExtensionEvent(trans, c, convertedArgs[0]));            
            Object result;
            try {
                result = c.newInstance(convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(new ExtensionEvent(trans, c, convertedArgs[0]));
            }
            return result;
        } else
            return c.newInstance(convertedArgs[0]);
      }
      else if (-1 != lastDot) {                         
        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Method m = (methodKey != null) ?
          (Method) getFromCache(methodKey, null, methodArgs) : null;
        if (m != null && !trans.getDebug())
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
            return m.invoke(null, convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
          }
        }
        className = m_className + funcName.substring(0, lastDot);
        methodName = funcName.substring(lastDot + 1);
        try
        {
          classObj = getClassForName(className);
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        m = MethodResolver.getMethod(classObj,
                                     methodName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     MethodResolver.STATIC_ONLY);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, m);
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, null, convertedArgs[0]);            
            Object result;
            try {
                result = m.invoke(null, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, null, convertedArgs[0]);
            }
            return result;
        }
        else
            return m.invoke(null, convertedArgs[0]);
      }
      else {                                            
        if (args.size() < 1)
        {
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_INSTANCE_MTHD_CALL_REQUIRES, new Object[]{funcName })); 
        }
        targetObject = args.get(0);
        if (targetObject instanceof XObject)          
          targetObject = ((XObject) targetObject).object();
        methodArgs = new Object[args.size() - 1];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i+1);
        }
        Method m = (methodKey != null) ?
          (Method) getFromCache(methodKey, targetObject, methodArgs) : null;
        if (m != null)
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
            return m.invoke(targetObject, convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
          }
        }
        classObj = targetObject.getClass();
        m = MethodResolver.getMethod(classObj,
                                     funcName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     MethodResolver.INSTANCE_ONLY);
        if (methodKey != null)
          putToCache(methodKey, targetObject, methodArgs, m);
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, targetObject, convertedArgs[0]);            
            Object result;
            try {
                result = m.invoke(targetObject, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, targetObject, convertedArgs[0]);
            }
            return result;
        } else       
            return m.invoke(targetObject, convertedArgs[0]);
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
  public void processElement (String localPart,
                              ElemTemplateElement element,
                              TransformerImpl transformer,
                              Stylesheet stylesheetTree,
                              Object methodKey)
    throws TransformerException, IOException
  {
    Object result = null;
    Class classObj;
    Method m = (Method) getFromCache(methodKey, null, null);
    if (null == m)
    {
      try
      {
        String fullName = m_className + localPart;
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot < 0)
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_ELEMENT_NAME, new Object[]{fullName })); 
        try
        {
          classObj = getClassForName(fullName.substring(0, lastDot));
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        localPart = fullName.substring(lastDot + 1);
        m = MethodResolver.getElementMethod(classObj, localPart);
        if (!Modifier.isStatic(m.getModifiers()))
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEMENT_NAME_METHOD_STATIC, new Object[]{fullName })); 
      }
      catch (Exception e)
      {
        throw new TransformerException (e);
      }
      putToCache(methodKey, null, null, m);
    }
    XSLProcessorContext xpc = new XSLProcessorContext(transformer, 
                                                      stylesheetTree);
    try
    {
      if (transformer.getDebug()) {
          transformer.getTraceManager().fireExtensionEvent(m, null, new Object[] {xpc, element});
        try {
            result = m.invoke(null, new Object[] {xpc, element});
        } catch (Exception e) {
            throw e;
        } finally {            
            transformer.getTraceManager().fireExtensionEndEvent(m, null, new Object[] {xpc, element});
        }
      } else
        result = m.invoke(null, new Object[] {xpc, element});
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
      throw new TransformerException (e);
    }
    if (result != null)
    {
      xpc.outputToResultTree (stylesheetTree, result);
    }
  }
}
