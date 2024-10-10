package com.sdk.helldroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    public static String newFunc(){
        return "Helldroid";
    }
    public static String newFunc2(){
        return "ART Hooked";
    }
}