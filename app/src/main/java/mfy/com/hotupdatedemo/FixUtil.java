package mfy.com.hotupdatedemo;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixUtil {
    public static void fix(Context context, String patchPath) {
        if (context == null) return;
        File fileDir = context.getDir(Const.DEX_DIR, Context.MODE_PRIVATE);
        //创建优化目录
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        doInject(context,
                patchPath,//dex补丁所在的路径，可以任意路径
                fileDir,//app内部路径,存放优化dex优化文件的路径
                Const.PATCH_NAME);
    }

    private static void doInject(Context appContext, String patchPath, File fileDir, String patchName) {

        //获取应用内部的类加载器
        PathClassLoader pathClassLoader = (PathClassLoader) appContext.getClassLoader();
        //实例化dexClassLoader用于加载补丁dex
        DexClassLoader dexClassLoader = new DexClassLoader(patchPath, fileDir.getAbsolutePath(), null, pathClassLoader);
        try {
            //获取dexclassloader和pathclassloader的dexpathlist
            Object dexPathList = getPathList(dexClassLoader);
            Object pathPathList = getPathList(pathClassLoader);
            //获取补丁的elements数组
            Object dexElements = getDexElements(dexPathList);
            //获取程序的elements
            Object pathElements = getDexElements(pathPathList);
            //合并两个数组
            Object resultElements = combineArray(dexElements, pathElements);
            //将合并后的数组设置给PathClassLoader
            setField(pathPathList, pathPathList.getClass(), "dexElements", resultElements);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void setField(Object pathPathList, Class<?> clazz, String fieldName, Object resultElements) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(pathPathList, resultElements);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> clazz = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = Array.getLength(arrayRhs) + i;
        Object result = Array.newInstance(clazz, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    //获得dexElements
    private static Object getDexElements(Object dexPathList) {
        return getField(dexPathList, dexPathList.getClass(), "dexElements");
    }

    //获得DexPathList
    private static Object getPathList(Object classLoader) throws ClassNotFoundException {
        return getField(classLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    //通过反射获取一个类私有属性的值
    private static Object getField(Object obj, Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
