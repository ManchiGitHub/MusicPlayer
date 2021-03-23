package com.markokatziv.musicplayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created By marko
 */
public class AnimRotationViewModel extends ViewModel {
    private boolean isPlaying = false;
    private final MutableLiveData<Boolean> isMusicPlayingMLD = new MutableLiveData<Boolean>();

    public void setIsMusicPlayingMLD(Boolean aBollean) {
        isMusicPlayingMLD.setValue(aBollean);
    }

    public LiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

    public void setPlaying(boolean b) {
        this.isPlaying = b;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}