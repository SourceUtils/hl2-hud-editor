package com.timepath.tf2.hudedit;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class StrippedClassLoader extends ClassLoader {
    
    public static final String tempSubdirectory = "ClassLoader";
    private File dirTemp;
    
    private List<JarFileInfo> loadedJars;
    private HashSet<File> deleteOnExit;
    
    public StrippedClassLoader() {
        this(ClassLoader.getSystemClassLoader());
    }
    
    public StrippedClassLoader(ClassLoader parent) {
        super(parent);
        
        loadedJars = new ArrayList<JarFileInfo>();
        deleteOnExit = new HashSet<File>();
        
        String sUrlTopJar = null;
        URL urlTopJar = getClass().getProtectionDomain().getCodeSource().getLocation();
        String protocol = urlTopJar.getProtocol();
        
        JarFileInfo jarFileInfo = null;
        if("file".equals(protocol)) {
            try {
                sUrlTopJar = URLDecoder.decode(urlTopJar.getFile(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return;
            }
            File fileJar = new File(sUrlTopJar);
            
            if(fileJar.isDirectory()) {
                return;
            }
            
            try {
                jarFileInfo = new JarFileInfo(new JarFile(fileJar), fileJar.getName(), null, null);
            } catch (IOException e) { 
                return;
            } 
        }
        
        try {
            if(jarFileInfo == null) {
                throw new IOException(String.format("Unknown protocol %s", protocol));
            }
            loadJar(jarFileInfo);
        } catch (IOException e) {
            return;
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }
    
    private void loadJar(JarFileInfo jarFileInfo) throws IOException {
        loadedJars.add(jarFileInfo);
        try {
            Enumeration<JarEntry> e = jarFileInfo.jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory()) {
                    continue;
                }
                String s = je.getName().toLowerCase(); // JarEntry name
                if(s.substring(s.length() - 4).equals(".jar")) { // When there isn't anything else trailing the ".jar"
                    JarEntryInfo inf = new JarEntryInfo(jarFileInfo, je);
                    File fileTemp = createTempFile(inf);
                    loadJar(new JarFileInfo(new JarFile(fileTemp), inf.getName(), jarFileInfo, fileTemp));
                }
            }
        } catch (MyClassLoaderException e) {
            throw new RuntimeException("ERROR on loading inner JAR: " + e.getMessageAll());
        }
    }
    
    /**
     * Called on shutdown to cleanup temporary files.
     * <p>
     * JVM does not close handles to native libraries files or JARs with
     * resources loaded as getResourceAsStream(). Temp files are not deleted
     * even if they are marked deleteOnExit(). They also fail to delete explicitly.
     * Workaround is to preserve list with temp files in configuration file
     * "[user.home]/.MyClassLoader" and delete them on next application run.
     * <p>
     * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4171239
     * "This occurs only on Win32, which does not allow a file to be deleted
     * until all streams on it have been closed."
     */
    private void shutdown() {
        for (JarFileInfo jarFileInfo : loadedJars) {
            try {
                jarFileInfo.jarFile.close();
            } catch (IOException e) {
            }
            File file = jarFileInfo.fileDeleteOnExit;
            if (file != null  &&  !file.delete()) {
                deleteOnExit.add(file);
            }
        }
        File fileCfg = new File(System.getProperty("user.home") + File.separator + ".MyClassLoader");
        deleteOldTemp(fileCfg);
        persistNewTemp(fileCfg);
    }

    //<editor-fold defaultstate="collapsed" desc="Delete temp files">
    private void deleteOldTemp(File fileCfg) {
        BufferedReader reader = null;
        try {
            int count = 0;
            reader = new BufferedReader(new FileReader(fileCfg));
            String sLine;
            while ((sLine = reader.readLine()) != null) {
                File file = new File(sLine);
                if (!file.exists()) {
                    continue;
                }
                if (file.delete()) {
                    count++;
                } else {
                    deleteOnExit.add(file);
                }
            }
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { }
            }
        }
    }
    
    private void persistNewTemp(File fileCfg) {
        if (deleteOnExit.isEmpty()) {
            fileCfg.delete(); // do not pollute disk
            return;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileCfg));
            for (File file : deleteOnExit) {
                if (!file.delete()) {
                    String f = file.getCanonicalPath();
                    writer.write(f);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) { }
            }
        }
    }
    //</editor-fold>
    
    public void invokeMain(String sClass, String[] args) throws Throwable {
        Method method = loadClass(sClass).getMethod("main", new Class<?>[] { String[].class });

        boolean modifiersValid = false;
        boolean returnTypeValid = false;

        if(method != null) {
            method.setAccessible(true);
            int modifiers = method.getModifiers(); // main() must be "public static"
            modifiersValid = Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
            Class<?> returnType = method.getReturnType(); // main() must be "void"
            returnTypeValid = (returnType == void.class);
        }
        if(method == null || !modifiersValid || !returnTypeValid) {
            throw new NoSuchMethodException("The main() method in class \"" + sClass + "\" not found.");
        }
        
        try {
            method.invoke(null, (Object)args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
    
    @Override
    protected String findLibrary(String sLib) {
        if(!loadedJars.isEmpty()) {
            JarEntryInfo inf = findJarNativeEntry(sLib);
            if (inf != null) {
                try {
                    File file = createTempFile(inf);
                    deleteOnExit.add(file);
                    return file.getAbsolutePath();
                } catch (MyClassLoaderException e) {
                }
            }
            return null;
        }
        return super.findLibrary(sLib);
    }
    
    /**
     * Finds native library entry.
     *
     * @param sLib Library name. For example for the library name "Native"
     * the Windows returns entry "Native.dll",
     * the Linux returns entry "libNative.so",
     * the Mac returns entry "libNative.jnilib".
     *
     * @return Native library entry.
     */
    private JarEntryInfo findJarNativeEntry(String sLib) {
        String sName = System.mapLibraryName(sLib);
        for (JarFileInfo jarFileInfo : loadedJars) {
            JarFile jarFile = jarFileInfo.jarFile;
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                if (je.isDirectory()) {
                    continue;
                }
                // Example: sName is "Native.dll"
                String sEntry = je.getName(); // "Native.dll" or "abc/xyz/Native.dll"
                // sName "Native.dll" could be found, for example
                //   - in the path: abc/Native.dll/xyz/my.dll <-- do not load this one!
                //   - in the partial name: abc/aNative.dll   <-- do not load this one!
                String[] token = sEntry.split("/"); // the last token is library name
                if (token.length > 0 && token[token.length - 1].equals(sName)) {
                    return new JarEntryInfo(jarFileInfo, je);
                }
            }
        }
        return null;
    }
    
    private File createTempFile(JarEntryInfo inf) throws MyClassLoaderException {
        if (dirTemp == null) {
            File dir = new File(System.getProperty("java.io.tmpdir"), tempSubdirectory);
            if(!dir.exists()) {
                dir.mkdir();
            }
            chmod777(dir);
            if(!dir.exists() || !dir.isDirectory()) {
                throw new MyClassLoaderException("Cannot create temp directory " + dir.getAbsolutePath());
            }
            dirTemp = dir;
        }
        File fileTmp = null;
        try {
            fileTmp = File.createTempFile(inf.getName() + ".", null, dirTemp);
            fileTmp.deleteOnExit();
            chmod777(fileTmp);
            byte[] a_by = inf.getJarBytes();
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileTmp));
            os.write(a_by);
            os.close();
            return fileTmp;
        } catch (IOException e) {
            throw new MyClassLoaderException(String.format("Cannot create temp file '%s' for %s", fileTmp, inf.jarEntry), e);
        }
    }
    
    private void chmod777(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }   
       
    //<editor-fold defaultstate="collapsed" desc="Internal classes">
    /**
     * Inner class with JAR file information.
     */
    private static class JarFileInfo {
        JarFile jarFile;   // this is the essence of JarFileInfo wrapper
        String simpleName; // accumulated for logging like: "topJar!childJar!kidJar"
        File fileDeleteOnExit;
        Manifest mf; // required for package creation
        
        /**
         * @param jarFile
         *            Never null.
         * @param simpleName
         *            Used for logging. Never null.
         * @param jarFileParent
         *            Used to make simpleName for logging. Null for top level JAR.
         * @param fileDeleteOnExit
         *            Used only to delete temporary file on exit.
         *            Could be null if not required to delete on exit (top level JAR)
         */
        JarFileInfo(JarFile jarFile, String simpleName, JarFileInfo jarFileParent, File fileDeleteOnExit) {
            this.simpleName = (jarFileParent == null ? "" : jarFileParent.simpleName + "!") + simpleName;
            this.jarFile = jarFile;
            this.fileDeleteOnExit = fileDeleteOnExit;
            try {
                this.mf = jarFile.getManifest(); // 'null' if META-INF directory is missing
            } catch (IOException e) {
                // Ignore and create blank manifest
            }
            if (this.mf == null) {
                this.mf = new Manifest();
            }
        }
        String getSpecificationTitle() {
            return mf.getMainAttributes().getValue(Name.SPECIFICATION_TITLE);
        }
        String getSpecificationVersion() {
            return mf.getMainAttributes().getValue(Name.SPECIFICATION_VERSION);
        }
        String getSpecificationVendor() {
            return mf.getMainAttributes().getValue(Name.SPECIFICATION_VENDOR);
        }
        String getImplementationTitle() {
            return mf.getMainAttributes().getValue(Name.IMPLEMENTATION_TITLE);
        }
        String getImplementationVersion() {
            return mf.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION);
        }
        String getImplementationVendor() {
            return mf.getMainAttributes().getValue(Name.IMPLEMENTATION_VENDOR);
        }
        URL getSealURL() {
            String seal = mf.getMainAttributes().getValue(Name.SEALED);
            if (seal != null) {
                try {
                    return new URL(seal);
                } catch (MalformedURLException e) {
                    // Ignore, will return null
                }
            }
            return null;
        }
    } // inner class JarFileInfo
    
    /**
     * Inner class with JAR entry information. Keeps JAR file and entry object.
     */
    private static class JarEntryInfo {
        JarFileInfo jarFileInfo;
        JarEntry jarEntry;
        JarEntryInfo(JarFileInfo jarFileInfo, JarEntry jarEntry) {
            this.jarFileInfo = jarFileInfo;
            this.jarEntry = jarEntry;
        }
        URL getURL() { // used in findResource() and findResources()
            try {
                return new URL("jar:file:" + jarFileInfo.jarFile.getName() + "!/" + jarEntry);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        String getName() { // used in createTempFile() and loadJar()
            return jarEntry.getName().replace('/', '_');
        }
        @Override
        public String toString() {
            return "JAR: " + jarFileInfo.jarFile.getName() + " ENTRY: " + jarEntry;
        }
        /**
         * Read JAR entry and returns byte array of this JAR entry. This is
         * a helper method to load JAR entry into temporary file.
         *
         * @param inf JAR entry information object
         * @return byte array for the specified JAR entry
         * @throws MyClassLoaderException
         */
        byte[] getJarBytes() throws MyClassLoaderException {
            DataInputStream dis = null;
            byte[] a_by = null;
            try {
                long lSize = jarEntry.getSize();
                if (lSize <= 0  ||  lSize >= Integer.MAX_VALUE) {
                    throw new MyClassLoaderException(
                            "Invalid size " + lSize + " for entry " + jarEntry);
                }
                a_by = new byte[(int)lSize];
                InputStream is = jarFileInfo.jarFile.getInputStream(jarEntry);
                dis = new DataInputStream(is);
                dis.readFully(a_by);
            } catch (IOException e) {
                throw new MyClassLoaderException(null, e);
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e) {
                    }
                }
            }
            return a_by;
        } // getJarBytes()
    } // inner class JarEntryInfo
    
    /**
     * Inner class to handle MyClassLoader exceptions.
     */
    @SuppressWarnings("serial")
    private static class MyClassLoaderException extends Exception {
        MyClassLoaderException(String sMsg) {
            super(sMsg);
        }
        MyClassLoaderException(String sMsg, Throwable eCause) {
            super(sMsg, eCause);
        }
        String getMessageAll() {
            StringBuilder sb = new StringBuilder();
            for (Throwable e = this;  e != null;  e = e.getCause()) {
                if (sb.length() > 0) {
                    sb.append(" / ");
                }
                String sMsg = e.getMessage();
                if (sMsg == null  ||  sMsg.length() == 0) {
                    sMsg = e.getClass().getSimpleName();
                }
                sb.append(sMsg);
            }
            return sb.toString();
        }
    }
    //</editor-fold>
}