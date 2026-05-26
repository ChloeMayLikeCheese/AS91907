/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Song class for setting up songs
Date: 26\05\2026
Notes are located at the bottom of the file
*/

package org.AS91907;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

public final class Song {
    private final File song;
    private final String title;
    private AdvancedPlayer player;

    // Contructors for setting up the song and the title
    public Song(String song) throws IOException, InvalidAudioFormatException {
        validateType(song);// Make sure type is an mp3
        this.song = new File(song);
        this.title = this.song.getName().replaceFirst("[.][^.]+$", ""); // Remove the file extension from the title

    }

    public Song(File song) throws InvalidAudioFormatException, IOException {
        validateType(song.getPath());
        this.song = song;
        this.title = song.getName().replaceFirst("[.][^.]+$", "");
    }

    // Function for validating the type of the file
    public void validateType(String songPathString) throws IOException, InvalidAudioFormatException {
        Path songPath = Paths.get(new File(songPathString).getPath()); // Define a java.nio.file path instead of a java.io.File path
        String type = Files.probeContentType(songPath); // Get the content type, See Note #1

        if (!type.strip().equals("audio/mpeg")) { // Make sure the content type is "mpeg" (pretty much just a mp3)
            throw new InvalidAudioFormatException("Error: Invalid MIME type: Expected audio/mpeg but got: " + type); // Throw an exception with an error message
        }
    }

    // Getters for the song file, title, path and the file name
    public File getFile() {
        return song;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return song.getName();
    }

    public String getPath() {
        return getFile().getPath();
    }

    public void play() throws FileNotFoundException, JavaLayerException, InterruptedException {
        FileInputStream stream = new FileInputStream(song);
        player = new AdvancedPlayer(stream);
        player.setPlayBackListener(new PlaybackListener() {
        });
        Thread playerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    player.play();
                    player.close();
                } catch (JavaLayerException e) {
                }
            }
        });
        playerThread.start();
    }

    public void stop() {
        player.stop();
    }

    public void pause(){
        
    }
}
/* 
Notes:
    Note #1
        Files.probeContentType() uses the JVMs inbuilt file type checkers to return a string in the form of a "MIME type", which is just the media type, for example for mp3s it would return "audio/mpeg".
    Source: https://stackoverflow.com/questions/25298691/how-to-check-the-file-type-in-java (http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType%28java.nio.file.Path%29 is located in one of the responses, which is what I used) 

*/