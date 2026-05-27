/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Class for creating and managing .m3u playlist files
Date: 27\05\2026
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

public class M3u implements AutoCloseable { // So I can use a try block with resources, and it will automatically close. Source: https://dev.to/ca5th/memory-leaks-in-java-and-how-to-avoid-them-48h3
    private String name;
    Boolean playlist;
    private File m3u;
    private File parentDir;
    private BufferedWriter m3uWriter; // BufferedWriter is just a more efficient way of writing to files, via and output stream

    // Constructor
    public M3u(String name) {
        this.name = name;
    }

    // Function for creating the file
    public void create() throws IOException {
        parentDir = new File("library");
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        m3u = new File(parentDir, name + ".m3u");
        m3u = new File(parentDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.flush();
    }

    public void createPlaylist() throws IOException {
        playlist = true;
        parentDir = new File("playlists/" + name);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        m3u = new File(parentDir, name + ".m3u");
        m3u = new File(parentDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.flush();
    }

    // Functions for adding a single song to the m3u file
    public void add(Song song) throws IOException {
        try {
            if (playlist = true) { // Check to see if the m3u file is a playlist, if so copy the added files to the playlist
                File dest = new File(parentDir.getPath() + "/" + song.getFileName()); // Create the destination file
                Path destPath = Paths.get(dest.getPath()); // Get the destination and source paths, but for java.nio.file 
                Path sourcePath = Paths.get(song.getPath());
                Files.copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES); // Copy the files over, Source: https://stackoverflow.com/questions/16433915/how-to-copy-file-from-one-location-to-another-location (I got COPY_ATTRIBUTES from reading the Docs here: https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#copy(java.nio.file.Path,%20java.nio.file.Path,%20java.nio.file.CopyOption...))

            }
        } catch (FileAlreadyExistsException e) {
        }
        m3uWriter.newLine();
        m3uWriter.newLine();
        m3uWriter.write("#EXTINF:60," + song.getTitle() + "\n" + song.getFileName()); // Write the song file to the m3u, I'll do formatting later 
        m3uWriter.flush();

    }

    public void add(File song) throws IOException, InvalidAudioFormatException {
        add(new Song(song));
    }

    public void add(String song) throws IOException, InvalidAudioFormatException {
        add(new Song(song));
    }

    // Functions for adding whole directories to the m3u file
    public void addAll(String path) throws IOException, InvalidAudioFormatException {
        addAll(new File(path));
    }

    public void addAll(File file) throws IOException, InvalidAudioFormatException {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                add(new Song(c));
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
    }
}
 