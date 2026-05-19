/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Main class for an MP3 player
Date: 19\05\2026
*/
package org.AS91907;

// Base imports
import java.io.IOException;
import java.net.URISyntaxException;

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
}
