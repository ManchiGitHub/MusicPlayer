package com.markokatziv.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;


/**
 * Created By marko katziv
 */
public class MyAnimations {

    ObjectAnimator animation;

    public MyAnimations() {
    }

    public void startMyAnimation(View v, float value) {

        /* If current language is not english, flip animation transition to fit current language. */
        if (!LanguageUtils.getCurrentLanguage().equals(LanguageUtils.ENGLISH)) {
            value *= -1;
        }

        animation = ObjectAnimator.ofFloat(v, "translationX", value);
        animation.setDuration(200);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation = ObjectAnimator.ofFloat(v, "translationX", 0);
                animation.start();
            }
        });

        animation.start();
    }
}
