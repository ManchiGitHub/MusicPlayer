package com.markokatziv.musicplayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created By marko
 */
public class MusicStateViewModel extends ViewModel {

//    private boolean isPlaying = false;
    private final MutableLiveData<Boolean> isMusicPlayingMLD = new MutableLiveData<Boolean>();
    private final MutableLiveData<Boolean> isSongReadyMLD = new MutableLiveData<Boolean>();
    private final MutableLiveData<Song> currentSongMLD = new MutableLiveData<Song>();
    private final MutableLiveData<Integer> songDurationMLD = new MutableLiveData<Integer>();

    public void setIsMusicPlayingMLD(Boolean aBoolean) {
        isMusicPlayingMLD.setValue(aBoolean);
      //  setPlaying(aBoolean);
    }


    public LiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

    public void setCurrentSong(Song song) {
        currentSongMLD.setValue(song);
    }

    public LiveData<Song> getCurrentSong() {

        return currentSongMLD;
    }

    public void setIsSongReadyMLD(boolean isReady){
        isSongReadyMLD.setValue(isReady);
    }

    public LiveData<Boolean> getIsSongReady(){
        return isSongReadyMLD;
    }

    public void setSongDuration(int integer) {
        songDurationMLD.setValue(integer);
    }

    public LiveData<Integer> getSongDuration(){
        return songDurationMLD;
    }


    //    public boolean isPlaying() {
//        return isPlaying;
//    }
//
//    private void setPlaying(boolean playing) {
//        isPlaying = playing;
//    }
}