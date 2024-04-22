package com.maozz.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 60224
 */
public class MySpringApplicationContext {

    private Class<?> configClass;

    private ConcurrentMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Object> singletonMap = new ConcurrentHashMap<>();

    public MySpringApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        // 扫描
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            // 扫描路径
            String path = configClass.getAnnotation(ComponentScan.class).value(); // 这一步得到的是包路径，但是需要扫描的是编译完的target下的路径下的文件
            ClassLoader classLoader = MySpringApplicationContext.class.getClassLoader();
            URL url = classLoader.getResource(path.replace(".", "/"));

            File files = new File(url.getFile());
            if (files.isDirectory()) {
                File[] files1 = files.listFiles();
                for (File file : files1) {
                    String absolutePath = file.getAbsolutePath();
                    if (absolutePath.endsWith(".class")) {
                        try {
                            String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class")).replace("\\", ".");
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                String beanName = clazz.getAnnotation(Component.class).value();
                                if ("".equals(beanName)) {
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        }

        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                singletonMap.put(beanName, createBean(beanName, beanDefinition));
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> type = beanDefinition.getType();


        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if ("singleton".equals(beanDefinition.getScope())) {
            Object bean = singletonMap.get(beanName);
            if (bean == null) {
                createBean(beanName, beanDefinition);
                singletonMap.put(beanName, bean);
            }
            return bean;
        } else {
            return createBean(beanName, beanDefinition);
        }
    }
}
