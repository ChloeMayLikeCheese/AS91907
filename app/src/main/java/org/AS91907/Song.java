/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Song class for setting up songs
Date: 02\06\2026
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

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public final class Song implements Runnable, AutoCloseable {
    private final File song;
    private String title;
    private String track;
    private String album;
    private String artist;
    private String year;
    private String genre;
    private long length;
    private AdvancedPlayer player; // For the music player I literally just read the docs, more in Note #2
    private volatile boolean playing = false; // A volatile boolean is just a boolean with memory visibilty so it can be accessed by other threads, see Note #3
    private volatile boolean paused = false;
    private Thread playerThread;
    private int lastPosition = 0;
    private final Object lock = new Object();
    private Mp3File mp3File;

    public Song(String songPath)
            throws IOException, InvalidAudioFormatException, UnsupportedTagException, InvalidDataException {
        this(new File(songPath));
    }

    public Song(File song)
            throws InvalidAudioFormatException, IOException, UnsupportedTagException, InvalidDataException {
        System.out.println("DEBUG: Trying to parse file -> " + song.getName());
        validateType(song.getPath());
        this.song = song;
        mp3File = new Mp3File(this.song);
        this.length = mp3File.getLengthInSeconds();

        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            this.title = id3v2Tag.getTitle();
            this.track = id3v2Tag.getTrack();
            this.album = id3v2Tag.getAlbum();
            this.artist = id3v2Tag.getArtist();
            this.year = id3v2Tag.getYear();
            this.genre = id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")";
        } else if (mp3File.hasId3v1Tag()) {
            ID3v1 id3v1Tag = mp3File.getId3v1Tag();
            this.title = id3v1Tag.getTitle();
            this.track = id3v1Tag.getTrack();
            this.album = id3v1Tag.getAlbum();
            this.artist = id3v1Tag.getArtist();
            this.year = id3v1Tag.getYear();
            this.genre = id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")";
        } else {
            this.title = song.getName().replaceFirst("[.][^.]+$", "");
        }
    }

    // Function for validating the type of the file
    private void validateType(String songPathString) throws IOException, InvalidAudioFormatException {
        Path songPath = Paths.get(new File(songPathString).getPath()); // Define a java.nio.file path instead of a java.io.File path
        String type = Files.probeContentType(songPath); // Get the content type, See Note #1

        if (!type.strip().equals("audio/mpeg")) { // Make sure the content type is "mpeg" (pretty much just a mp3)
            throw new InvalidAudioFormatException("Error: Invalid MIME type: Expected audio/mpeg but got: " + type); // Throw an exception with an error message
        }
    }

    @Override
    public void run() { //See Note #3
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

    public String getData() throws IOException, UnsupportedTagException, InvalidDataException {
        String tagData;
        //mp3File = new Mp3File(this.song);
        StringBuilder tagDataBuilder = new StringBuilder();
        if (mp3File.hasId3v2Tag()) {
            // ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            tagDataBuilder.append(getTitle()).append("\n");
            tagDataBuilder.append(getTrack()).append("\n");
            tagDataBuilder.append(getAlbum()).append("\n");
            tagDataBuilder.append(getArtist()).append("\n");
            tagDataBuilder.append(getYear()).append("\n");
            tagDataBuilder.append(getGenre()).append("\n");
        }
        tagDataBuilder.append(mp3File.getLengthInSeconds()).append("\n");
        tagData = tagDataBuilder.toString();
        return tagData;
    }

    public String getTrack() {
        return track;
    }

    public String getArtist() {
        return artist;
    }

    public String getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public long getLength() {
        return length;
    }

    public String getAlbum() {
        return album;
    }
}

/* 
Notes:
    Note #1
        Files.probeContentType() uses the JVMs inbuilt file type checkers to return a string in the form of a "MIME type", which is just the media type, for example for mp3s it would return "audio/mpeg".
    Source: https://stackoverflow.com/questions/25298691/how-to-check-the-file-type-in-java (http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType%28java.nio.file.Path%29 is located in one of the responses, which is what I used) 
    Note #2

    Source: https://javadoc.io/doc/javazoom/jlayer/latest/index.html
    Note #3

    Sources: https://stackoverflow.com/questions/16758346/how-pause-and-then-resume-a-thread, https://stackoverflow.com/questions/106591/what-is-the-volatile-keyword-useful-for, https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html

*/
