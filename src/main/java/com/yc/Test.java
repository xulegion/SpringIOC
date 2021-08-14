package com.yc;

import com.yc.service.ProductServiceImpl;
import com.yc.springframework.context.YcAnnotationConfigApplicationContext;
import com.yc.springframework.context.YcApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class Test {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        YcApplicationContext applicationContext = new YcAnnotationConfigApplicationContext(Appconfig.class);

//        Set<Class> annotatedClassSet = ((YcAnnotationConfigApplicationContext) applicationContext).getAnnotatedClassSet();
//        for (Class clazz : annotatedClassSet) {
//            System.out.println(clazz);
//        }

        ProductServiceImpl productServiceImpl = (ProductServiceImpl) applicationContext.getBean("psi");
        ProductServiceImpl productServiceImpl2 = (ProductServiceImpl) applicationContext.getBean("psi");

        System.out.println(productServiceImpl);
        System.out.println(productServiceImpl2);
        productServiceImpl.add();


    }
}
