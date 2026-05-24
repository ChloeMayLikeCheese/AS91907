/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Main class for an MP3 player
Date: 25\05\2026
Notes are located at the bottom of the file
*/
package org.AS91907;

//Imports
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

public class Main {
    public static File library = new File("library/");
    public static File playlists = new File("playlists/");

    enum Operation {
        DEBUG_CLEAR
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, URISyntaxException, InvalidAudioFormatException {
        //checkNativeAccess(args);
        try (Terminal terminal = TerminalBuilder.builder() // Create the terminal
                .name("mp3 player") // Name the terminal
                .jansi(true) // Enable jansi for compatibility with ANSI for windows
                .build()) { // Build the terminal
            terminal.enterRawMode(); // Set the terminal to raw input mode, so it is always listening to input
            BindingReader bindingReader = new BindingReader(terminal.reader()); // Set up the BindingReader
            KeyMap<Operation> keyMap = new KeyMap<>(); // Set up the KeyMap
            // KeyMap bindings
            keyMap.bind(Operation.DEBUG_CLEAR, "C");
            // Main input loop
            boolean isReading = true;
            while (isReading) {
                if (!library.exists()) {
                    library.mkdir();
                }
                if (!playlists.exists()) {
                    playlists.mkdir();
                }
                terminal.puts(Capability.clear_screen); // Clear the screen
                terminal.writer().flush();
                Operation op = bindingReader.readBinding(keyMap, null, false); // Read the keybindings
                if (op != null) {
                    switch (op) {
                    case DEBUG_CLEAR -> {
                        deleteDir(library);
                        deleteDir(playlists);
                    }
                    }
                }
            }

        }
    }

    // To clear an annoying warning that appears when you run the .jar without native access enabled, added in recent versions for presumably security reasons
    // See note #1 for details
    public static void checkNativeAccess(String[] args) throws URISyntaxException, InterruptedException, IOException {
        // Check if native access is enabled by checking the java -jar arguments via the runtime environment
        boolean nativeAccessEnabled = ManagementFactory.getRuntimeMXBean().getInputArguments().stream() // See note #1.1
                .anyMatch(arg -> arg.contains("--enable-native-access"));

        if (nativeAccessEnabled) { // Just return if native access is enabled
            return;
        }
        System.out.println("Native Access is not enabled. Restarting with native access enabled...");
        // Get the path to the JAR
        File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()); // See note #1.2
        String jarPath = jarFile.getAbsolutePath(); // Convert to string

        // Create the command for restarting the application with native access enabled
        List<String> command = new ArrayList<>(); // All the arguments must be in seperate strings
        command.add("java");
        command.add("--enable-native-access=ALL-UNNAMED");
        command.add("-jar");
        command.add(jarPath);

        // Start the new process and exit the current one
        ProcessBuilder pb = new ProcessBuilder(command).inheritIO(); // Create the process
        Process process = pb.start(); //Start it
        System.out.println("Restarting...");
        System.exit(process.waitFor()); // Terminate current process and make the new process wait until the old one is terminated

    }

    // Delete a directory and its contents
    public static void deleteDir(File file) {
        if (file.isDirectory()) { // Check if the file passed is a directory
            for (File c : file.listFiles()) { // Recursively delete its contents
                if (c.isDirectory()) { // Check to see if any of the contents are a directory, if so call deleteDir() recursively
                    deleteDir(c);
                } else {
                    c.delete();
                }
            }
            file.delete(); // Delete the directory
        }
    }

}

/* 
Notes:
    Note #1:
            #1.1ManagementFactory
                ManagementFactory.getRuntimeMXBean() looks inside the JVM to see details about how the program was run. 
                The .getInputArguments() function retrives the arguments that the program was run with.
                The .stream() function orders and outputs ("streams") the arguments in a way that java can look at them efficiently.
                The .anyMatch() function searches the streamed data for any conditions passed in the functions arguments
                The 'arg -> arg.contains("--enable-native-access")' argument passed in the .anyMatch() function is a condition that checks the the input arguments of the program and checks wether or not it contains the --enable-native-access flag.
                Sources: https://stackoverflow.com/questions/1518213/read-java-jvm-startup-parameters-eg-xmx/1518250#1518250

            #1.2    
                Main.class.getProtectionDomain() gets the ProtectionDomain of the main class, a ProtectionDomain contains information about where the class is and its permissions.
                This is used as it gets the full path to the jar (or any runtime environment) that its running from.
                The .getCodeSource() function then returns the source of the ProtectionDomain and .getLocation() gets just the location of it.
                The .toURI() function properly formats it for use as a file path by handling spaces, special characters and things like that.
                Sources: https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
        I have used pretty much the exact same function for modifying the runtime enviroment in previous projects, but as many original sources as I can find have been provided. 
*/