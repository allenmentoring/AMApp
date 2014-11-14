package com.allenmentoring.allenmentoring;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Allen on 11/10/14.
 */
public class AllenMentoringApplication extends Application {

    @Override
    public void onCreate() {
        Parse.initialize(this, "ddJTB6rdtKyp6BEChESmQl5Ix8hHLtFN9PimL3H6", "J2ij3ny1hIWCun58t4oq7yh2BNIffkDnj70I3C97");

    }
}
