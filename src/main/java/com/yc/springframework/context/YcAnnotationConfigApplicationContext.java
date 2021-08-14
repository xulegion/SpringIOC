package com.yc.springframework.context;

import com.yc.springframework.annotations.*;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YcAnnotationConfigApplicationContext implements YcApplicationContext {

    private ConcurrentHashMap<String,Object> beanMap=new ConcurrentHashMap<>();

    private Set<Class> annotatedClassSet=new HashSet<>();

//    public Set<Class> getAnnotatedClassSet() {
//        return annotatedClassSet;
//    }

    @Override
    public Object getBean(String beanId) {
        return beanMap.get(beanId);
    }


    public YcAnnotationConfigApplicationContext(Class<?> componentClasses) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        this.register(componentClasses);
    }

    private void register(Class<?> componentClasses) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        //TODO:读取配置类，解析@ComponentScan中的包名，扫描里面带了@Yccomponent...这些，完成IOC
        //1.取出AppConfig上的 @YcComponentScan(basePackages = "org.example") 的basePackags
        if (componentClasses.getAnnotation(YcConfiguration.class)==null){
            //判断传进来的类是否为配置类
            throw new RuntimeException(componentClasses.getName()+" is not a valid configuration class");
        }
        String basePath=parseComponentScan(componentClasses);

        //2.扫描这个路径下所有的类，取出带有 IOC注解的类，存好classSet
        doScan(basePath);


        //3.IOC
        doIoc();
        //4.DI
        doDi();
    }

    private void doDi() throws InvocationTargetException, IllegalAccessException {
        //被托管的类才能Di
        if (annotatedClassSet==null || annotatedClassSet.size()<=0){
            return;
        }
        for (Class c:annotatedClassSet){
            Method[] ms = c.getMethods();
            if (ms==null || ms.length<=0){
                continue;
            }
            for (Method m:ms){
                if (m.isAnnotationPresent(YcResource.class)){
                    YcResource yr = m.getAnnotation(YcResource.class);
                    String toDiBeanId = yr.name();
                    Object toDiObj = beanMap.get(toDiBeanId);
                    Object target = getObjectFromBeanMap(c);
                    m.invoke(target,toDiObj);
                }
            }
        }
    }

    private Object getObjectFromBeanMap(Class c) {
        Object obj=null;
        obj=beanMap.get(c.getSimpleName().substring(0,1).toLowerCase()+c.getSimpleName().substring(1));
        if (obj==null){
            Collection<Object> collection = beanMap.values();
            for (Object o:collection){
                if (o.getClass().getName().equals(c.getName())){
                    obj=o;
                    break;
                }
            }
        }
        return obj;
    }

    private void doIoc() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (annotatedClassSet ==null && annotatedClassSet.size()<=0){
            return;
        }
        //集合遍历
        for (Class c:annotatedClassSet){
            //取出beandId
            String beanId=getBeanId(c);
            if (beanMap.contains(beanId)){
                continue;
            }
            Object obj = c.newInstance();
            //查看是否有@PostConstruct
            handlePostConstruct(c,obj);
            beanMap.put(beanId,obj);
        }
    }

    //处理@PostConstruct
    private void handlePostConstruct(Class c, Object obj) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = c.getMethods();
        if (methods!=null && methods.length>0){
            for (Method m:methods){
                if (m.isAnnotationPresent(YcPostConstruct.class)){
                    //激活此方法
                    m.invoke(obj);
                    break;
                }
            }
        }
    }

    //取出每个托管bean的id
    private String getBeanId(Class c) {
        //情况1: 1.没有value配置，则将类名首字母小写作为 beanid
        String beanId="";
        if (c.isAnnotationPresent(YcComponent.class)){
            beanId=((YcComponent)c.getAnnotation(YcComponent.class)).value();
        }else  if (c.isAnnotationPresent(YcController.class)){
            beanId=((YcController)c.getAnnotation(YcController.class)).value();
        }else  if (c.isAnnotationPresent(YcService.class)){
            beanId=((YcService)c.getAnnotation(YcService.class)).value();
        }else  if (c.isAnnotationPresent(YcRepository.class)){
            beanId=((YcRepository)c.getAnnotation(YcRepository.class)).value();
        }

        if ("".equals(beanId)){
            // 2.有value，则取出作为beanid
            String className = c.getSimpleName();
            beanId=className.substring(0,1).toLowerCase()+className.substring(1);
        }
        return beanId;
    }

    private void doScan(String basePath) {
        //从basePath下加载字节码(子包 --> 递归) com.yc
        String bp = basePath.replaceAll("\\.", "/");
        URL resource = this.getClass().getClassLoader().getResource(bp);
        System.out.println("待扫描的URL  绝对路径:"+resource); //file:/D:/JavaSSM(YC)/8.13/ycSpring/target/classes/com/yc
//        System.out.println(resource.getPath());  ///D:/JavaSSM(YC)/8.13/ycSpring/target/classes/com/yc3
        File file = new File(resource.getPath());
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                System.out.println("待判断的文件为:"+pathname);
                //加入一个规则
                if (pathname.isDirectory()){
                    System.out.println(pathname+"是一个目录，要继续递归下一级..."+(basePath+"."+pathname.getName()));
//                    System.out.println(pathname.getPath());
                    doScan(basePath+"."+pathname.getName());
                }else {
                    //是文件，则要判断是否为 .class
                    if (pathname.getName().endsWith(".class")){
                        //com.yc.biz.ProductServiceImpl
                        String classPath=basePath+"."+pathname.getName().replaceAll("\\.class","");
//                        System.out.println("找到了class文件:"+classPath);
                        Class clazz=null;
                        try {
                            clazz=Class.forName(classPath);
                            if (clazz.isAnnotationPresent(YcComponent.class)||
                                    clazz.isAnnotationPresent(YcService.class)||
                                    clazz.isAnnotationPresent(YcRepository.class)||
                                    clazz.isAnnotationPresent(YcController.class)){
                                annotatedClassSet.add(clazz);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }

    private String parseComponentScan(Class<?> componentClasses) {
        String basePath="";
        YcComponentScan annotation = componentClasses.getAnnotation(YcComponentScan.class);
        if (annotation==null){
            return basePath;
        }
        basePath = annotation.basePackage();
        System.out.println("待扫描的路径为:"+basePath);
        return basePath;
    }

}
