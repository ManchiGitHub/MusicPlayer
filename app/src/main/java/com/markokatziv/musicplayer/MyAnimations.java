package com.markokatziv.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

/**
 * Created By marko katziv
 */
public class MyAnimations {

    //TODO: Do I really need this?
    public static void AnimateFavoriteButton(View v) {

        ScaleAnimation scaleUpAnimation = new ScaleAnimation(1f, 1.1f, 1f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        ScaleAnimation scaleDownAnimation = new ScaleAnimation(1.1f, 1f, 1.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);

        scaleUpAnimation.setDuration(200);
        scaleDownAnimation.setDuration(200);

        scaleUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(scaleDownAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(scaleUpAnimation);


    }

    public static void AnimateBackAndPrevBtns(View v, float value) {

        ObjectAnimator animation;
        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

        /* If current language is not english, flip animation transition to fit current language. */
        if (LanguageUtils.getCurrentLanguage() != LanguageUtils.EN_LANGUAGE) {
            value *= -1;
        }

        animation = ObjectAnimator.ofFloat(v, "translationX", value);
        animation.setDuration(70);
        animation.setInterpolator(accelerateInterpolator);
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
