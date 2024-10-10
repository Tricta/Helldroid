package com.lib.helldroid;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Helldroid extends AppCompatActivity {

    private static native void nativeRegister(Class<?> clazz);

    public static void registerNativeMethods(Class<?> clazz){
        nativeRegister(clazz);
    }

    public static void createDummyFile(Context context){
        String fileName = "ModFinder";
        String moduleName = "awk";

        File file = new File(context.getExternalFilesDir(null), fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(moduleName.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkMainTrace(){
        /*for (StackTraceElement element : MainStackTrace) {
            Log.d("IntegrityCheck", element.getClassName() + "." + element.getMethodName());
        }*/

        List<String> currentStackTrace = Arrays.stream(MainStackTrace)
                .map(element -> element.getClassName() + "." + element.getMethodName())
                .collect(Collectors.toList());

        if (!KNOWN_INVOKE_STACK_TRACE.stream().allMatch(currentStackTrace::contains)) {
            System.exit(-1);
        }
    }

    public static void LoadLibrary(Context context, Class<?> activity) {
        try {
            String libName = "helldroid";
            Method loadLibrary = Runtime.class.getDeclaredMethod("loadLibrary0", ClassLoader.class, String.class);
            loadLibrary.setAccessible(true);
            loadLibrary.invoke(Runtime.getRuntime(), activity.getClassLoader(), libName);

            Method registerNative = Helldroid.class.getDeclaredMethod("registerNativeMethods", Class.class);
            registerNative.setAccessible(true);
            registerNative.invoke(null, activity);

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            /*for (StackTraceElement element : stackTrace) {
                Log.d("IntegrityCheck", element.getClassName() + "." + element.getMethodName());
            }*/

            List<String> currentStackTrace = Arrays.stream(stackTrace)
                    .map(element -> element.getClassName() + "." + element.getMethodName())
                    .collect(Collectors.toList());

            if (!KNOWN_LIB_STACK_TRACE.stream().allMatch(currentStackTrace::contains)) {
                System.exit(-1);
            }
        } catch (UnsatisfiedLinkError | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            ((Activity) context).finish();
        }
    }

    public static StackTraceElement[] MainStackTrace;

    private static final List<String> KNOWN_LIB_STACK_TRACE = Arrays.asList(
            "com.lib.helldroid.Helldroid.LoadLibrary",
            "java.lang.reflect.Method.invoke"
    );

    private static final List<String> KNOWN_INVOKE_STACK_TRACE = Arrays.asList(
            "java.lang.reflect.Method.invoke"
    );
}
