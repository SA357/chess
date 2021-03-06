package com.network.client.applicationGUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class Music {
    public static Media RMusic = new Media(new File("music\\Служить России.mp3").toURI().toString());
    public static Media standard = new Media(new File("music\\Stalingrad, Massengrab - Alle sieben Sekunden stirbt ein deutscher Soldat.mp3").toURI().toString());
    public static Media admin = Math.random() * 100 < 68
            ? new Media(new File("music\\Narodowy.mp3").toURI().toString())
            : new Media(new File("music\\09. iXioR75 - Samotny Lowca.mp3").toURI().toString());
    public static Media nashid = new Media(new File("music\\Nasheed.mp3").toURI().toString());

    public static MediaPlayer turnMusic(Media media){
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);//mediaPlayer.play();
        mediaPlayer.setVolume(0.3);
        //mediaPlayer.setRate(1.02);
        mediaPlayer.setOnEndOfMedia( () -> mediaPlayer.seek(Duration.ZERO) );
        return mediaPlayer;
    }
}
