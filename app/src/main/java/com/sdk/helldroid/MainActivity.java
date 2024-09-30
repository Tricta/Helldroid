package com.sdk.helldroid;

import static com.sdk.helldroid.Helldroid.MainStackTrace;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.lib.hellcrypt.hellcrypt;

public class MainActivity extends Activity {

    public native void ReplaceMethodByObject(Object targetObject, Object newObject);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hellcrypt.init();

        try {
            Method hell = Helldroid.class.getDeclaredMethod("LoadLibrary", Context.class);
            hell.setAccessible(true);

            hell.invoke(null, this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            this.finish();
        }

        try {
            Method targetMethod = Helldroid.class.getDeclaredMethod("createDummyFile", Context.class);
            Method replacementMethod = Helldroid.class.getDeclaredMethod("checkMainTrace");

            Method replaceMethod = MainActivity.class.getDeclaredMethod("ReplaceMethodByObject", Object.class, Object.class);

            replaceMethod.setAccessible(true);
            replaceMethod.invoke(this, targetMethod, replacementMethod);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            this.finish();
        }

        /*try {
            Method targetMethod = MainActivity.class.getDeclaredMethod("newFunc");
            Method replacementMethod = MainActivity.class.getDeclaredMethod("newFunc2");

            Method replaceMethod = MainActivity.class.getDeclaredMethod("ReplaceMethodByObject", Object.class, Object.class);

            replaceMethod.setAccessible(true);
            replaceMethod.invoke(this, targetMethod, replacementMethod);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            this.finish();
        }*/

        setContentView(R.layout.activity_main);

        TextView tv = (TextView)findViewById(R.id.sample_text);
        tv.setText(newFunc());

        try {
            Method checkIntegrity = Helldroid.class.getDeclaredMethod("createDummyFile", Context.class);
            checkIntegrity.setAccessible(true);

            MainStackTrace = Thread.currentThread().getStackTrace();

            checkIntegrity.invoke(null, this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            this.finish();
        }
    }

    public static String newFunc(){
        return "Helldroid";
    }
    public static String newFunc2(){
        return "ART Hooked";
    }
}