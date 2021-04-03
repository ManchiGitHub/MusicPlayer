package com.markokatziv.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

/**
 * Created By marko katziv
 */
public class MyAnimations {

    ObjectAnimator animation;
    int value;

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
