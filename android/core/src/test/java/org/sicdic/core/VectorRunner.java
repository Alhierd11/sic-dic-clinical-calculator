package org.sicdic.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Dependency-free test executable; fails the process on any mismatched golden vector. */
public final class VectorRunner {
    private VectorRunner() {}
    public static void main(String[] args) throws Exception {
        int count=0;
        StringBuilder output=new StringBuilder();
        for (String line : Files.readAllLines(Path.of(args[0]),StandardCharsets.UTF_8)) {
            String[] fields=line.split("\t",-1);
            String actual=Calculator.evaluateVector(fields[1],Arrays.copyOfRange(fields,2,fields.length-1));
            String expected=fields[fields.length-1];
            if (!expected.equals(actual)) throw new AssertionError(fields[0]+": expected "+expected+" but got "+actual);
            output.append(fields[0]).append('\t').append(actual).append('\n');
            count++;
        }
        if (args.length>1) Files.writeString(Path.of(args[1]),output,StandardCharsets.UTF_8);
        System.out.println("PASS: "+count+" shared golden vectors; rules "+Rules.VERSION);
    }
}
