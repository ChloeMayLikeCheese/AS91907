/*
Author: Chloe T (https://github.com/ChloeMayLikeCheese)
Purpose: Class for a custom exception, for when the audio format is invalid. It's pretty redundent for this project but I wanted to learn how.
Date: 02\06\2026
*/
package org.AS91907;

public class InvalidAudioFormatException extends Exception { // Source: https://www.baeldung.com/java-new-custom-exception
    
    // Constructors for the exception
    public InvalidAudioFormatException() {
    }

    // Constructor but it can take a message
    public InvalidAudioFormatException(String message) {
        super(message);
    }
}
