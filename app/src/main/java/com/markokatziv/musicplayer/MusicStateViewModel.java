package com.markokatziv.musicplayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created By marko
 */
public class MusicStateViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isMusicPlayingMLD = new MutableLiveData<Boolean>();

    public void setIsMusicPlayingMLD(Boolean aBoolean) {
        isMusicPlayingMLD.setValue(aBoolean);
    }

    public LiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

}