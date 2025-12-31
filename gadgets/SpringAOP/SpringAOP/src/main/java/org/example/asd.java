package org.example;

import java.lang.reflect.Method;
import java.util.HashMap;

public class asd {
    public static void main(String[] args) throws Exception{
        Method method = HashMap.class.getMethod("get", String.class);
        System.out.println(method);
        Object result = method.invoke("asd");  // 相当于 target.getName()
        System.out.println(result);
    }
}
