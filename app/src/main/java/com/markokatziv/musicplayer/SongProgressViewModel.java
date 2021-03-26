package com.markokatziv.musicplayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created By marko
 */
public class SongProgressViewModel extends ViewModel {

    private final MutableLiveData<Integer> songProgress = new MutableLiveData<Integer>();

    public void setSongProgressMLD(Integer progress) {
        songProgress.setValue(progress);
    }

    public LiveData<Integer> getSongProgressMLD() {
        return songProgress;
    }

}