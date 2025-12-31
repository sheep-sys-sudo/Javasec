package org.example;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAdvisorChainFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.aop.target.SingletonTargetSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.Main.allocateInstance;
import static org.example.Main.setField;

public class Test {
    public static void main(String[] args) throws Exception {
        Class<?> jdkDynamicProxyClass = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        Constructor<?> proxyConstructor = jdkDynamicProxyClass.getDeclaredConstructor(AdvisedSupport.class);
        proxyConstructor.setAccessible(true);

        AdvisedSupport advisedSupport = new AdvisedSupport();

        advisedSupport.setTargetSource(new SingletonTargetSource("target"));

        List<Advisor> advisors = new ArrayList<>();

        AspectJAroundAdvice advice = (AspectJAroundAdvice) allocateInstance(AspectJAroundAdvice.class);

        //NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor((Advice)getBProxy(advice, new Class[]{MethodInterceptor.class, Advice.class}));
        NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor(advice);
        advisor.setMappedName("toString");
        advisors.add(advisor);

//
        setField(advisedSupport, "advisors", advisors);
        setField(advisedSupport, "advisorChainFactory", new DefaultAdvisorChainFactory());

        InvocationHandler handler = (InvocationHandler) proxyConstructor.newInstance(advisedSupport);

        Object proxy = Proxy.newProxyInstance(
                Main.class.getClassLoader(),
                new Class[]{Map.class},
                handler
        );
        proxy.toString();
    }
    public static Object getBProxy(Object obj,Class[] clazzs) throws Exception
    {
        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setTarget(obj);
        Constructor constructor = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy").getConstructor(AdvisedSupport.class);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(advisedSupport);
        Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), clazzs, handler);
        return proxy;
    }
}
