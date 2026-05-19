/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Main class for an MP3 player
Date: 19\05\2026
*/
package org.AS91907;

// Base imports
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
    enum Operation {
        TEST
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        checkNativeAccess(args);
        try (Terminal terminal = TerminalBuilder.builder() // Create the terminal
                .name("mp3 player") // Name the terminal
                .jansi(true) // Enable jansi for compatibility with ANSI for windows
                .build()) { // Build the terminal
            terminal.enterRawMode(); // Set the terminal to raw input mode, so it is always listening to input
            BindingReader bindingReader = new BindingReader(terminal.reader()); // Set up the BindingReader
            KeyMap<Operation> keyMap = new KeyMap<>(); // Set up the KeyMap
            // KeyMap bindings
            keyMap.bind(Operation.TEST, " ", "i");

            // Main input loop
            boolean isReading = true;
            while (isReading) {
                terminal.puts(Capability.clear_screen); // Clear the screen

                Operation op = bindingReader.readBinding(keyMap, null, false); // Read the keybindings
                if (op != null) {
                    switch (op) {
                    case TEST -> {
                        terminal.writer().println("Hello World");
                        terminal.writer().flush();
                        Thread.sleep(1000);
                    }
                    }
                }
            }

        }
    }

    // To clear an annoying warning that appears when you run the .jar without native access enabled, added in recent versions for presumably security reasons? im unsure
    public static void checkNativeAccess(String[] args) throws URISyntaxException, InterruptedException, IOException {
        // Check if native access is enabled by checking the java -jar arguments
        boolean nativeAccessEnabled = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(arg -> arg.contains("--enable-native-access"));

        if (nativeAccessEnabled) { // Just return if native access is enabled
            return;
        }
        System.out.println("Native Access is not enabled. Restarting with native access enabled...");
        // Get the path to the JAR
        java.io.File jarFile = new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String jarPath = jarFile.getAbsolutePath();

        // Create the command for restarting the application with native access enabled
        List<String> command = new ArrayList<>(); // All the arguments must be in seperate strings
        command.add("java");
        command.add("--enable-native-access=ALL-UNNAMED");
        command.add("-jar");
        command.add(jarPath);

        // Start the new process and exit the current one
        ProcessBuilder pb = new ProcessBuilder(command).inheritIO();
        Process process = pb.start();
        System.out.println("Restarted");
        System.exit(process.waitFor());

    }
}
