/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Class for creating and managing .m3u playlist files
Date: 17\06\2026
*/
package org.AS91907;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class M3u implements AutoCloseable { // So I can use a try block with resources, and it will automatically close. Source: https://dev.to/ca5th/memory-leaks-in-java-and-how-to-avoid-them-48h3
    private String name;
    boolean playlist = false;
    private File m3u;
    private File parentDir;
    private BufferedWriter m3uWriter; // BufferedWriter is just a more efficient way of writing to files, via an output stream
    private final List<Song> playlistSongs = new ArrayList<>();
    // Constructor

    public M3u(String name) {
        this.name = name;
    }

    // Function for creating the file
    public void create(String parentDirPath) throws IOException {
        parentDir = new File(parentDirPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        m3u = new File(parentDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.flush();
    }

    public void createPlaylist() throws IOException {
        playlist = true;
        parentDir = new File("library/playlists/" + name);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        m3u = new File(parentDir, name + ".m3u");
        m3uWriter = new BufferedWriter(new FileWriter(m3u));
        m3uWriter.write("#EXTM3U");
        m3uWriter.newLine();
        m3uWriter.write("#PLAYLIST:" + name);
        m3uWriter.flush();

    }

    // Functions for adding a single song to the m3u file

    public void add(Song song)
            throws IOException, UnsupportedTagException, InvalidDataException, InvalidAudioFormatException {
        if (song != null) {
            System.err.println("DEBUG: M3U: Adding song..." + song.getFileName());
            playlistSongs.add(song);
            System.err.println("DEBUG: M3U: Added song" + song.getFileName());
        }
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

    private void writeSongEntry(Song song, String sourcePath)
            throws IOException, UnsupportedTagException, InvalidDataException {
        StringBuilder songDataBuilder = new StringBuilder();
        songDataBuilder.append("#EXTINF:");
        songDataBuilder.append(song.getLength()).append(",");
        songDataBuilder.append(song.getArtist()).append(" - ");
        songDataBuilder.append(song.getTitle()).append("\n");
        String songData = songDataBuilder.toString();

        m3uWriter.newLine();
        m3uWriter.newLine();
        m3uWriter.write(songData);
        m3uWriter.write(song.getPath());
        m3uWriter.flush();
    }

    @Override // @Override allows me to call the close() function from the interface AutoCloseable, and then implement my own behavior on top of that 
    public void close() throws IOException, UnsupportedTagException, InvalidDataException, InvalidAudioFormatException { // Auto close the writer after its finsihed so it doesn't cause a memory leak
        try {
            if (playlist) {
                for (Song song : playlistSongs) { // If no artist, clear the track number so I can properly sort it with the rest of the artistless songs 
                    if (song.getArtist().equalsIgnoreCase("UnknownArtist")) {
                        song.setTrack("UnknownTrack");
                    }

                    else if ((song.getAlbum().equalsIgnoreCase("UnknownAlbum")) // If no album but has track, clear the track, TODO: maybe keep the track if it has an album
                            && !song.getTrack().equalsIgnoreCase("UnknownTrack")) {
                        song.setTrack("UnknownTrack");
                    }
                }

                playlistSongs.sort(new Comparator<Song>() { // Create a new Comparator for sorting the songs, See Note #1.1
                    @Override // @Override so I can override the compare() function from the comparator
                    public int compare(Song s1, Song s2) {
                        // Push songs with unknown artists to the bottom of the playlist
                        boolean isUnknownA1 = s1.getArtist().equals("UnknownArtist");
                        boolean isUnknownA2 = s2.getArtist().equals("UnknownArtist");
                        if (isUnknownA1 != isUnknownA2) {
                            return isUnknownA1 ? 1 : -1; // Using a ternary operator (pretty much just a shorthand if-else), return wether or not the song is unknown or not, See Note #1.2
                        }
                        // Group songs by artist
                        if (!isUnknownA1) {
                            int artistCompare = s1.getArtist().compareToIgnoreCase(s2.getArtist()); // Compare one artist to another for sorting
                            if (artistCompare != 0)
                                return artistCompare; // If they arent the same, return the comparison, See Note #1.3
                        }
                        // Push songs with unknown album to the bottom of the artist group
                        boolean isUnknownAl1 = s1.getAlbum().equals("UnknownAlbum");
                        boolean isUnknownAl2 = s2.getAlbum().equals("UnknownAlbum");
                        if (isUnknownAl1 != isUnknownAl2) {
                            return isUnknownAl1 ? 1 : -1; // Check if album is unknown
                        }
                        if (!isUnknownAl1) {
                            int albumCompare = s1.getAlbum().compareToIgnoreCase(s2.getAlbum()); // Compare one album to another for sorting 
                            if (albumCompare != 0)
                                return albumCompare; // Return the comparison
                        }

                        // Push songs with unknown track to the bottom of the album group, logic explained in the previous comments
                        boolean isUnknownT1 = s1.getTrack().equals("00");
                        boolean isUnknownT2 = s2.getTrack().equals("00");
                        if (isUnknownT1 != isUnknownT2) {
                            return isUnknownT1 ? 1 : -1;
                        }
                        if (!isUnknownT1) {
                            return s1.getTrack().compareTo(s2.getTrack());
                        }

                        return s1.getTitle().compareToIgnoreCase(s2.getTitle()); // Fallback to sorting by title if the data is the same
                    }
                });

                // Assign tracks to artistless songs
                int artistlessTrackCounter = 1;
                for (Song song : playlistSongs) {
                    if (song.getArtist().equals("UnknownArtist")) {
                        song.setTrack(String.format("%02d", artistlessTrackCounter++));
                    }
                }
            }

            // Processing file transfers and adding songs to the M3u
            for (Song song : playlistSongs) {
                File sourceFile = new File(song.getPath()); // Get the source file

                if (playlist) {
                    try (Song playlistSong = new Song(sourceFile)) { // Write the song to the M3u
                        writeSongEntry(playlistSong, sourceFile.getPath());
                    }


                    /*                     
                    Code for creating a new mp3 with a formatted file name in the playlist file, realized it was redundent.

                    String songFileName = String.format("%s_%s_%s_%s.mp3", song.getArtist(), song.getAlbum(),
                            song.getTrack(), song.getTitle()); // Format the song file name
                    File dest = new File(parentDir.getPath() + "/" + songFileName); // Get the destination file for copying
                    
                    if (dest.exists()) {
                        try (Song tmp = new Song(dest)) {
                            if (!tmp.getData().equals(song.getData())) { // Handle renaming the song file if there are duplicates of the same song with different data
                                System.err.println(
                                        "DEBUG: Renaming file with same name but different data: " + dest.getName());
                                int i = 0;
                                while (dest.exists()) {
                                    i++;
                                    String alternativeTitle = song.getTitle() + String.format("%02d", i); // Add a number to the file name, to signify that its a different song
                                    songFileName = String.format("%s_%s_%s_%s.mp3", song.getArtist(), song.getAlbum(),
                                            song.getTrack(), alternativeTitle);
                                    dest = new File(parentDir.getPath() + "/" + songFileName);
                                }
                                song.setTitle(song.getTitle() + String.format("%02d", i));
                            } else {
                                System.err.println("DEBUG: Deleting already existing file: " + dest.getName());
                                dest.delete(); // Delete any duplicates with the same data and title
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    if (!dest.exists()) {
                        System.err.println("DEBUG: M3U: Copying data to new file" + dest.getName());
                        Path destPath = Paths.get(dest.getPath()); // Convert to paths for Java.nio.File
                        Path sourcePath = Paths.get(song.getPath());
                        Files.copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES); // Copy the song data over to the newly created fi;e
                    } */

                }
            }

        } finally {
            // Close all the resources
            for (Song song : playlistSongs) {
                if (song != null) {
                    song.close();
                    System.err.println("DEBUG: M3U: Closed song used for comparing");
                }
            }
            if (m3uWriter != null) {
                m3uWriter.close();
                System.err.println("DEBUG: M3UWRITER: Closed");
            }

            System.err.println("DEBUG: M3U: Closed");
        }
    }
}
