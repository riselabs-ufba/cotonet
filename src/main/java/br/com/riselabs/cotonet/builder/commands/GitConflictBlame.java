/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Olaf Lessenich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.builder.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.riselabs.cotonet.model.beans.ChunkBlame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;

/**
 * @author Olaf Lessenich
 * @author Alcemir R. Santos
 */
public class GitConflictBlame {

    private static final String CONFLICT_START = "<<<<<<<";
    private static final String CONFLICT_SEP = "=======";
    private static final String CONFLICT_END = ">>>>>>>";

    // -e prints the email addresses, -n the original line numbers
    private static final String BLAME_CMD = "git blame -e -n";

    // we need this to disable the pager
    private static final String[] BLAME_ENV = {"GIT_PAGER=cat"};

    public static HashMap<String, HashMap<String, List<List<Integer>>>> blameChunks(File conflictFile) throws IOException {
        /*
         * Track location by using the following encoding for the values:
         * -1 = out of conflict
         *  0 = in variant1
         *  1 = in variant2
         */
        int location = -1;

        // no octopus merges supported for now ;)
        String[] revisions = new String[2];

        HashMap<String, HashMap<String, List<List<Integer>>>> result = new HashMap<>();
        HashMap<String, List<Integer>> chunkAuthors = new HashMap<>();

        // run blame
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(BLAME_CMD + " " + conflictFile,
                BLAME_ENV, conflictFile.getParentFile());

        // parse output
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;

        List<ChunkBlame> blames = new ArrayList<ChunkBlame>();
        CommandLineBlameResult blameResult = new CommandLineBlameResult(conflictFile.getCanonicalPath());
        
        while ((line = buf.readLine()) != null) {
            // hack lines by blame into useful output
            line = line.replaceAll("^[a-fA-F0-9]+?\\s+?([0-9]+?)\\s+?\\(<(.+?)>.+?\\)(.+?)", "$2:$1:$3");
            String[] splitLine = line.split(":", 3);
            String author = splitLine[0];
            Integer number = Integer.valueOf(splitLine[1]);
            String content = splitLine[2].trim();

            // control flow seems a bit odd, I did this to collect the revision names before
            // pushing the authors into the hashmap

            if (content.startsWith(CONFLICT_START)) {
                location = 0;
                revisions[0] = content.split(" ", 2)[1];
                continue;
            } else if (content.startsWith(CONFLICT_SEP)) {
                location = 1;
            } else if (content.startsWith(CONFLICT_END)) {
                revisions[1] = content.split(" ", 2)[1];
                location = -1;
            } else if (location >= 0) {
                // we are in one of the conflicting chunks

                List<Integer> authorLines = chunkAuthors.containsKey(author)
                        ? chunkAuthors.get(author)
                        : new ArrayList<>();
                authorLines.add(number);
                chunkAuthors.put(author, authorLines);
                
                blameResult.addLineAuthor(number, author);
                blameResult.addLineContent(number, splitLine[2]);
                continue;
            } else {
                continue;
            }

            // we are at separator or end of a conflict, processing authors found in chunk

            String revision = location == -1 ? revisions[1] : revisions[0];
            result.putIfAbsent(revision, new HashMap<>());
            HashMap<String, List<List<Integer>>> authors = result.get(revision);

            blames.add( new ChunkBlame(revision, blameResult));
            
            for (Map.Entry<String, List<Integer>> entry : chunkAuthors.entrySet()) {
                String chunkAuthor = entry.getKey();
                List<List<Integer>> authorLines = authors.containsKey(chunkAuthor)
                        ? authors.get(chunkAuthor)
                        : new ArrayList<>();
                authorLines.add(entry.getValue());
                authors.put(chunkAuthor, authorLines);
            }

            chunkAuthors.clear();

        }

        buf.close();

        if (pr.exitValue() != 0) {
            buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            buf.lines().forEach(System.err::println);
            buf.close();
            throw new RuntimeException(String.format("Error on external call with exit code %d",
                    pr.exitValue()));
        }

        pr.getInputStream().close();
        pr.getErrorStream().close();
        pr.getOutputStream().close();

        return result;
    }
    
    public static List<ChunkBlame> getConflictingLinesBlames(File conflictFile) throws IOException {
        /*
         * Track location by using the following encoding for the values:
         * -1 = out of conflict
         *  0 = in variant1
         *  1 = in variant2
         */
        int location = -1;

        // no octopus merges supported for now ;)
        String[] revisions = new String[2];

        // run blame
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(BLAME_CMD + " " + conflictFile,
                BLAME_ENV, conflictFile.getParentFile());

        // parse output
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;

        List<ChunkBlame> blames = new ArrayList<ChunkBlame>();
        CommandLineBlameResult blameResult = new CommandLineBlameResult(conflictFile.getCanonicalPath());
        
        while ((line = buf.readLine()) != null) {
            // hack lines by blame into useful output
            line = line.replaceAll("^[a-fA-F0-9]+?\\s+?([0-9]+?)\\s+?\\(<(.+?)>.+?\\)(.+?)", "$2:$1:$3");
            String[] splitLine = line.split(":", 3);
            String author = splitLine[0];
            Integer number = Integer.valueOf(splitLine[1]);
            String content = splitLine[2].trim();

            // control flow seems a bit odd, I did this to collect the revision names before
            // pushing the authors into the hashmap

            if (content.startsWith(CONFLICT_START)) {
                location = 0;
                revisions[0] = content.split(" ", 2)[1];
                continue;
            } else if (content.startsWith(CONFLICT_SEP)) {
                location = 1;
            } else if (content.startsWith(CONFLICT_END)) {
                revisions[1] = content.split(" ", 2)[1];
                location = -1;
            } else if (location >= 0) {
                // we are in one of the conflicting chunks
                blameResult.addLineAuthor(number, author);
                blameResult.addLineContent(number, splitLine[2]);
                continue;
            } else {
                continue;
            }

            // we are at separator or end of a conflict, processing authors found in chunk

            String revision = location == -1 ? revisions[1] : revisions[0];
            blames.add( new ChunkBlame(revision, blameResult));
            blameResult = new CommandLineBlameResult(conflictFile.getCanonicalPath());
            
        }

        buf.close();

        if (pr.exitValue() != 0) {
            buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            buf.lines().forEach(System.err::println);
            buf.close();
            throw new RuntimeException(String.format("Error on external call with exit code %d",
                    pr.exitValue()));
        }

        pr.getInputStream().close();
        pr.getErrorStream().close();
        pr.getOutputStream().close();

        return blames;
    }

    public static HashMap<String, HashMap<String, List<Integer>>> blameFile(File conflictFile) throws IOException {
        HashMap<String, HashMap<String, List<List<Integer>>>> chunkResult = blameChunks(conflictFile);
        HashMap<String, HashMap<String, List<Integer>>> fileResult = new HashMap<>();

        // just aggregate chunks
        for (String revision : chunkResult.keySet()) {
            HashMap<String, List<Integer>> authors = new HashMap<>();
            HashMap<String, List<List<Integer>>> chunkMap = chunkResult.get(revision);


            for (String author : chunkMap.keySet()) {
                List<Integer> authorLines = new ArrayList<>();
                chunkMap.get(author).forEach(authorLines::addAll);
                authors.put(author, authorLines);
            }

            fileResult.put(revision, authors);
        }

        return fileResult;
    }
}