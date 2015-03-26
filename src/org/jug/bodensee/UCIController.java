/*
 * Copyright (c) 2015, JUG Bodensee
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *    following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and 
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jug.bodensee;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 *
 * @author sven
 */
public class UCIController {

    private final String path;
    private Process process;
    private InputStreamReader inputReader;
    private OutputStreamWriter outputStream;
    
    private final ReadOnlyStringWrapper lastLineFromEngine = new ReadOnlyStringWrapper();
    
    
    public UCIController(String path) {
        this.path = path;
    }

    public void init() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(path);
        process = processBuilder.start();
        inputReader = new InputStreamReader(process.getInputStream());

        Executors.newCachedThreadPool().submit(() -> {
            Scanner scan = new Scanner(inputReader);
            while (scan.hasNextLine()) {
                String newLine = scan.nextLine();
                System.out.println(newLine);
                lastLineFromEngine.setValue(newLine);
            }
        });

        outputStream = new OutputStreamWriter(process.getOutputStream());
        send("uci");
    }

    public String getLastLineFromEngine() {
        return lastLineFromEngine.get();
    }

    public ReadOnlyStringProperty lastLineFromEngineProperty() {
        return lastLineFromEngine.getReadOnlyProperty();
    }
    
    public void startNewGame() {
        send("ucinewgame");
    }

    public void go() {
        send("go inifinite");
    }
    
    private void send(String s) {
        try {
            System.out.println("SENDING TO ENGINE: " + s);
            outputStream.write(s + "\n");
            outputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(UCIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        if (null != process) {
            process.destroyForcibly();
        }
    }

}
