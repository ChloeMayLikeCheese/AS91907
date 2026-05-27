/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Song class for setting up songs
Date: 27\05\2026
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
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public final class Song implements Runnable, AutoCloseable {
    private final File song;
    private final String title;
    private AdvancedPlayer player; // For the music player I literally just read the docs, more in note #2
    private volatile boolean playing = false; // A volatile boolean is just a boolean with memory visibilty so it can be accessed by other threads
    private volatile boolean paused = false;
    private Thread playerThread;
    private int lastPosition = 0;
    private final Object lock = new Object();

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

    @Override
    public void run() { // Source: https://stackoverflow.com/questions/16758346/how-pause-and-then-resume-a-thread
        while (playing) {
            try {
                synchronized (lock) {
                    while (paused && playing) {
                        lock.wait();
                    }
                }
                FileInputStream stream = new FileInputStream(song);
                player = new AdvancedPlayer(stream);

                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent event) {
                        lastPosition += event.getFrame();
                    }
                });
                player.play(lastPosition, Integer.MAX_VALUE);

                if (!paused) {
                    playing = false;
                    lastPosition = 0;

                }
            } catch (JavaLayerException | FileNotFoundException | InterruptedException e) {
                playing = false;
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        synchronized (lock) {
            playing = false;
            paused = false;
            lastPosition = 0;
            if (player != null) {
                player.close();
            }
            lock.notifyAll();
        }
    }

    public void play() {
        synchronized (lock) {
            if (!playing) {
                playing = true;
                paused = false;
                playerThread = new Thread(this);
                playerThread.start();
            } else if (paused) {
                paused = false;
                lock.notifyAll();
            }
        }
    }

    public void pause() {
        synchronized (lock) {
            if (playing && !paused) {
                paused = true;
                if (player != null) {
                    player.close();
                }
            }
        }
    }

    @Override // @Override allows me to call the close() function from the interface AutoCloseable, and then implement my own behavior on top of that 
    public void close() throws IOException { // Auto close the player after its finsihed so it doesn't cause a memory leak
        if (player != null) {
            stop();
        }
    }
}

/* 
Notes:
    Note #1
        Files.probeContentType() uses the JVMs inbuilt file type checkers to return a string in the form of a "MIME type", which is just the media type, for example for mp3s it would return "audio/mpeg".
    Source: https://stackoverflow.com/questions/25298691/how-to-check-the-file-type-in-java (http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType%28java.nio.file.Path%29 is located in one of the responses, which is what I used) 

*/
