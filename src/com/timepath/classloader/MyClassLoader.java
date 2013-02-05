package com.timepath.classloader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class MyClassLoader extends ClassLoader {
    
    private static final Logger logger = Logger.getLogger(MyClassLoader.class.getName());
    
    MyClassLoader() {
        this(ClassLoader.getSystemClassLoader());
    }
    
    MyClassLoader(ClassLoader parent) {
        super(parent);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                
            }
        });
    }
    
    public void run(String className, String[] args) throws Throwable {
        Class<?> clazz = loadClass(className);
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("main", new Class<?>[] {String[].class});
        // ensure 'method' is 'public static void main(args[])'
        boolean modifiersValid = false;
        boolean returnTypeValid = false;
        if(method != null) {
            method.setAccessible(true);
            int modifiers = method.getModifiers();
            modifiersValid = Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
            Class<?> returnType = method.getReturnType();
            returnTypeValid = (returnType == void.class);
        }
        if(method == null || !modifiersValid || !returnTypeValid) {
            throw new NoSuchMethodException("Class \"" + className + "\" does not have a main method.");
        }
        
        try {
            method.invoke(instance, (Object) args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("findClass("+name+")");
        return super.findClass(name);
    }

    @Override
    protected String findLibrary(String libname) {
        System.out.println("findLibrary("+libname+")");
        return super.findLibrary(libname);
    }
    
    private void chmod777(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }
    
}