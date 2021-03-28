package com.markokatziv.musicplayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created By marko
 */
public class MusicStateViewModel extends ViewModel {

    private boolean isPlaying = false;
    private final MutableLiveData<Boolean> isMusicPlayingMLD = new MutableLiveData<Boolean>();

    public void setIsMusicPlayingMLD(Boolean aBoolean) {
        isMusicPlayingMLD.setValue(aBoolean);
        setPlaying(aBoolean);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public LiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

}