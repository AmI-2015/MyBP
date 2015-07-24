package com.dev.ami2015.mybikeplace;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * Created by Zephyr on 24/07/2015.
 */

public class MyTransformation extends PasswordTransformationMethod {

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;
        public PasswordCharSequence(CharSequence source) {
            mSource = source; // Store char sequence
        }
        public char charAt(int index) {
            //This is the check which makes sure the last character is shown
            if(index != mSource.length()-1)
                return '*';
            else
                return mSource.charAt(index);
        }
        public int length() {
            return mSource.length(); // Return default
        }
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end); // Return default
        }
    }
}
