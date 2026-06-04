/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Class for creating and managing .m3u playlist files
Date: 05\06\2026
*/
package org.AS91907;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class M3u implements AutoCloseable { // So I can use a try block with resources, and it will automatically close. Source: https://dev.to/ca5th/memory-leaks-in-java-and-how-to-avoid-them-48h3
    private String name;
    Boolean playlist;
    private File m3u;
    private File sourceM3u;
    private File parentDir;
    private File m3uDir;
    private BufferedWriter m3uWriter; // BufferedWriter is just a more efficient way of writing to files, via an output stream
    private BufferedWriter sourceM3uWriter;
    private int i = 0;
    private int j = 0;
    // Constructor

    public M3u(String name) {
        this.name = name;
    }

    // Function for creating the file
    public void create(String parentDirPath) throws IOException {
        this.parentDir = new File(parentDirPath);
        m3uDir = new File(parentDirPath + "/m3us");
        if (!this.parentDir.exists()) {
            this.parentDir.mkdirs();
        }
        if (!m3uDir.exists()) {
            m3uDir.mkdirs();
        }
        m3u = new File(m3uDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.flush();

        sourceM3u = new File(m3uDir, name + "_SOURCE.m3u");
        sourceM3uWriter = new BufferedWriter(new FileWriter(sourceM3u));
        sourceM3uWriter.write("#EXTM3U");
        sourceM3uWriter.flush();
    }

    public void createPlaylist() throws IOException {
        playlist = true;
        parentDir = new File("playlists/" + name);
        m3uDir = new File(parentDir + "/m3us");
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!m3uDir.exists()) {
            m3uDir.mkdirs();
        }
        m3u = new File(m3uDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.newLine();
        m3uWriter.write("#PLAYLIST:" + name);
        m3uWriter.flush();

        sourceM3u = new File(m3uDir, name + "_SOURCE.m3u");
        sourceM3uWriter = new BufferedWriter(new FileWriter(sourceM3u));
        sourceM3uWriter.write("#EXTM3U");
        sourceM3uWriter.newLine();
        sourceM3uWriter.write("#PLAYLIST:" + name);
        sourceM3uWriter.flush();
    }

    // Functions for adding a single song to the m3u file
    public void add(Song song)
            throws IOException, UnsupportedTagException, InvalidDataException, InvalidAudioFormatException {
        
        File sourceFile = new File(song.getPath());
        for (File c : parentDir.listFiles()) {
            
        }
        try {
            String songFileName = String.format("%s_%s_%s_%s.mp3", song.getArtist(), song.getAlbum(), song.getTrack(),
                    song.getTitle());
            if (playlist) { // Check to see if the m3u file is a playlist, if so copy the added files to the playlist
                File dest = new File(parentDir.getPath() + "/" + songFileName); // Create the destination file
                if (dest.exists()) {
                    try (Song tmp = new Song(dest)) {
                        if (!tmp.getData().equals(song.getData())) {
                            String str = songFileName;
                            System.err.println(
                                    "DEBUG: Renaming file with same name but differnt data: " + dest.getName());
                            for (File c : parentDir.listFiles()) {
                                if (!c.isDirectory()) {
                                    if (c.getName().equals(songFileName)) {
                                        i++;
                                        song.setTitle(song.getTitle() + String.format("%02d", i));
                                        songFileName = String.format("%s_%s_%s_%s.mp3", song.getArtist(),
                                                song.getAlbum(), song.getTrack(), song.getTitle());
                                        dest = new File(parentDir.getPath() + "/" + songFileName);
                                    }
                                }
                            }
                        } else {
                            System.err.println("DEBUG: Deleting already existing file: " + dest.getName());
                            dest.delete();
                        }
                    }
                }
                Path destPath = Paths.get(dest.getPath()); // Get the destination and source paths, but for java.nio.file 
                Path sourcePath = Paths.get(song.getPath());
                Files.copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES); // Copy the files over, Source: https://stackoverflow.com/questions/16433915/how-to-copy-file-from-one-location-to-another-location (I got COPY_ATTRIBUTES from reading the Docs here: https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#copy(java.nio.file.Path,%20java.nio.file.Path,%20java.nio.file.CopyOption...))
                song = new Song(dest);
            }
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
        StringBuilder songDataBuilder = new StringBuilder();
        songDataBuilder.append("#EXTINF:");
        songDataBuilder.append(song.getLength()).append(",");
        songDataBuilder.append(song.getArtist()).append(" - ");
        songDataBuilder.append(song.getTitle()).append("\n");
        String songData = songDataBuilder.toString();
        m3uWriter.newLine();
        m3uWriter.newLine();
        m3uWriter.write(songData); // Write the song file to the m3u, I'll do formatting later 
        m3uWriter.write(song.getPath());
        m3uWriter.flush();

        sourceM3uWriter.newLine();
        sourceM3uWriter.newLine();
        sourceM3uWriter.write(songData);
        sourceM3uWriter.write(sourceFile.getPath());
        sourceM3uWriter.flush();
    }

    public void add(File song)
            throws IOException, InvalidAudioFormatException, UnsupportedTagException, InvalidDataException {
        add(new Song(song));
    }

    public void add(String song)
            throws IOException, InvalidAudioFormatException, UnsupportedTagException, InvalidDataException {
        add(new Song(song));
    }

    // Functions for adding whole directories to the m3u file
    public void addAll(String path)
            throws IOException, InvalidAudioFormatException, UnsupportedTagException, InvalidDataException {
        addAll(new File(path));
    }

    public void addAll(File file)
            throws IOException, InvalidAudioFormatException, UnsupportedTagException, InvalidDataException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File c : files) {
                    try {
                        add(new Song(c));
                    } catch (IllegalArgumentException e) {
                        System.err.println("DEBUG: Skipped unreadable file inside directory: " + c.getName());
                    }
                }
            }
        }
    }

    // Function for getting all the data stored in the m3u
    public String getData() throws IOException {
        if (m3u == null || !m3u.exists()) { // If it doesnt exist or is empty just return nothing
            return "";
        }
        StringBuilder data = new StringBuilder(); // Set up a StringBuilder for getting the data
        try (BufferedReader reader = new BufferedReader(new FileReader(m3u))) { // Use a BufferedReader to read the data
            String readData;
            while ((readData = reader.readLine()) != null) { // Iterate through the data and add it to the StringBuilder
                if (data.length() > 0)
                    data.append("\n");
                data.append(readData);
            }
        }
        return data.toString(); // Return the data back as a string
    }

    @Override // @Override allows me to call the close() function from the interface AutoCloseable, and then implement my own behavior on top of that 
    public void close() throws IOException { // Auto close the writer after its finsihed so it doesn't cause a memory leak
        if (m3uWriter != null) {
            m3uWriter.close();
        }
        if (sourceM3uWriter != null) {
            sourceM3uWriter.close();
        }
    }
}
