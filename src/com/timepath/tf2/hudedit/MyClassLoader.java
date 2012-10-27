package com.timepath.tf2.hudedit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

/**
 *
 * @author andrew
 */
public class MyClassLoader extends ClassLoader {
    
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
        Method method = this.loadClass(className).getMethod("main", new Class<?>[] {String[].class});
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
            Thread.currentThread().setContextClassLoader(this);
            method.invoke(null, (Object)args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
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