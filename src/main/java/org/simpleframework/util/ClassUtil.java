package org.simpleframework.util;


import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public class ClassUtil {

    public static final String FILE_PROTOCOL = "file";

    /**
     * 获取包下类集合
     * @param packageName 包名
     * @return 类集合
     */
   public static Set<Class<?>> extractPackageClass(String packageName) {

       //1、获取到类的加载器
       ClassLoader classLoader = getClassLoader();

       //2、通过类加载器获取到加载的资源
       URL url = classLoader.getResource(packageName.replace(".","/"));
       if (url == null) {
           log.warn("unable to retrieve anything from package" + packageName);
           return null;
       }

       //3、依据不同的资源类型，采用不同的方式获取资源的集合
       Set<Class<?>> classSet = null;
       //过滤出文件类型的资源
       if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
           classSet = new HashSet<Class<?>>();
           File packageDirectory = new File(url.getPath());
           extratClassFile(classSet, packageDirectory, packageName);
       }
       return classSet;
   }

    /**
     * 递归获取目标package里面的所有class文件（包括子package里的class文件）
     * @param emptyClassSet
     * @param fileSource
     * @param packageName
     */
    private static void extratClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
        //如果不是一个文件夹就返回
        if (!fileSource.isDirectory()) {
            return;
        }
        //如果是一个文件夹，则调用其listFiles方法获取文件夹下的文件或者文件夹
        File[] files = fileSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }else {
                    //获取文件的绝对值路径
                    String absoluteFilePath = file.getAbsolutePath();
                    if (absoluteFilePath.endsWith(".class")) {
                        //若是class文件，则直接加载
                        addToClassSet(absoluteFilePath);
                    }
                }
                return false;
            }

            //根据class文件的绝对值路径，获取并生成class对象，并放入classSet中
            private void addToClassSet(String absoluteFilePath) {
                //1、从class文件的绝对值路径中提取出包含package的类名
                absoluteFilePath = absoluteFilePath.replace(File.separator,".");
                String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className = className.substring(0,className.lastIndexOf("."));

                //2、通过反射机制获取对应的Class对象并放入classSet中
                Class<?> targetClass = loadClass(className);
                emptyClassSet.add(targetClass);
            }
        });
        if (files != null) {
            for(File f : files) {
                extratClassFile(emptyClassSet, f, packageName);
            }
        }
   }

    /**
     * 获取classloader
     * @return 当前的classloader
     */
   public static ClassLoader getClassLoader() {

       return Thread.currentThread().getContextClassLoader();
   }

   public static Class<?> loadClass(String className) {
       try {
           return Class.forName(className);
       } catch (ClassNotFoundException e) {
           log.error("load class error:",e);
           throw new RuntimeException(e);
       }
   }


   public static <T> T newInstance(Class<?> clazz, boolean accessible) {
       try {
           Constructor constructor = clazz.getDeclaredConstructor();
           constructor.setAccessible(accessible);

           return (T)constructor.newInstance();
       } catch (Exception e) {
           log.error("newInstance error",e);
           throw new RuntimeException(e);
       }
   }

    /**
     * 设置类的属性值
     *
     * @param field      成员变量
     * @param target     类实例
     * @param value      成员变量的值
     * @param accessible 是否允许设置私有属性
     */
    public static void setField(Field field, Object target, Object value, boolean accessible){
        field.setAccessible(accessible);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.error("setField error", e);
            throw new RuntimeException(e);
        }
    }


}
