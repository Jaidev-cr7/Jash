import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.Widget;
import org.jline.reader.impl.DefaultParser;
import org.jline.utils.InfoCmp;
public class Main {

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        for(int i = 0;i<input.length();i++){
            char c = input.charAt(i);
            if(c == '\'' && !inDoubleQuotes){
                inSingleQuotes = !inSingleQuotes; 
            }else if( Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes){
                if(current.length() > 0){
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            }else if (c == '\\') {
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (inSingleQuotes) {
                        current.append('\\');
                    } else if (inDoubleQuotes) {
                        if (next == '"' || next == '\\' || next == '$' || next == '`') {
                            current.append(next);
                            i++;
                            continue; 
                        } else {
                            current.append('\\');
                            continue;   
                        }
                    } else {
                        current.append(next);
                        i++;
                        continue;       
                    }
                }
            }else if (c == '"' && !inSingleQuotes) {
                    inDoubleQuotes = !inDoubleQuotes;
            }else if (c == '>') {
                    if (i + 1 < input.length() && input.charAt(i + 1) == '>') {
                        if (current.toString().equals("1")) {
                            tokens.add("1>>");
                        }else if (current.toString().equals("2")) {
                            tokens.add("2>>");
                        }else {
                            if (current.length() > 0)
                                tokens.add(current.toString());
                            tokens.add(">>");
                        }
                        current.setLength(0);
                        i++;        
                    } else {
                        if (current.toString().equals("1")) {
                            tokens.add("1>");
                        } else if (current.toString().equals("2")) {
                            tokens.add("2>");
                        } else {
                            if (current.length() > 0)
                                tokens.add(current.toString());
                            tokens.add(">");
                        }
                        current.setLength(0);
                    }
                }else{
                current.append(c);
            }
        }
        if(current.length() > 0){
            tokens.add(current.toString());
        }
        return tokens;
    }
    
    static class ShellCompleter implements Completer {
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();
        for (String builtin : List.of("echo","exit","type","pwd","cd")) {
            if (builtin.startsWith(word)) {
                candidates.add(new Candidate(
                        builtin,
                        builtin,
                        null,
                        null,
                        " ",
                        null,
                        true
                    ));
            }
        }
        
        String path = System.getenv("PATH");
        Set<String> seen = new LinkedHashSet<>();
        for (String dir : path.split(File.pathSeparator)) {
            File folder = new File(dir);
            File[] files = folder.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (f.isFile() && f.canExecute() && f.getName().startsWith(word)) {
                    seen.add(f.getName());
                }
            }
        }
        for (String name : seen) {
            candidates.add(new Candidate(name));
        }
    }
}
    private static String longestCommonPrefix(Set<String> matches) {
        if (matches.isEmpty()) {
            return "";
        }

        String prefix = matches.iterator().next();

        for (String match : matches) {
            int i = 0;
            int limit = Math.min(prefix.length(), match.length());

            while (i < limit && prefix.charAt(i) == match.charAt(i)) {
                i++;
            }

            prefix = prefix.substring(0, i);

            if (prefix.isEmpty()) {
                break;
            }
        }

        return prefix;
    }
    public static void main(String[] args) throws Exception {
        Completer completer = new ShellCompleter();
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(new char[0]); 
        LineReader reader = LineReaderBuilder.builder().parser(parser).completer(completer).build();
        Widget original = reader.getWidgets().get("complete-word");
        final boolean[] firstTab = {true};
        Widget myTab = () -> {
            String prefix = reader.getBuffer().toString();
            int lastSpace = prefix.lastIndexOf(' ');
            if (lastSpace >= 0) {
                String filePrefix = prefix.substring(lastSpace + 1);
                int lastSlash = filePrefix.lastIndexOf('/');
                String directoryPath;
                String namePrefix;
                if(lastSlash >=0){
                    directoryPath = filePrefix.substring(0, lastSlash + 1);
                    namePrefix = filePrefix.substring(lastSlash+1);
                }else{
                    directoryPath = "";
                    namePrefix = filePrefix;
                }
                File searchDirectory;
                if(directoryPath.isEmpty()){
                    searchDirectory = new File(System.getProperty("user.dir"));
                }else{
                    searchDirectory = new File(System.getProperty("user.dir"), directoryPath);
                }
                File[] files = searchDirectory.listFiles();
                if (files != null) {
                    List<String> fileMatches = new ArrayList<>();
                    for (File file : files) {
                        if (file.getName().startsWith(namePrefix)) {
                            fileMatches.add(file.getName());
                        }
                    }
                    if (fileMatches.size() == 1) {
                        File matchedFile = new File(
                        searchDirectory,
                        fileMatches.get(0)
                    );
                    String completedPath = directoryPath + fileMatches.get(0);
                    reader.getBuffer().backspace(filePrefix.length());
                    if (matchedFile.isDirectory()) {
                        reader.getBuffer().write(completedPath + "/");
                    } else {
                        reader.getBuffer().write(completedPath + " ");
                    }
                    reader.getTerminal().flush();
                    firstTab[0] = true;
                    return true;
                    }
                    if (fileMatches.isEmpty()) {
                        reader.getTerminal().puts(InfoCmp.Capability.bell);
                        reader.getTerminal().flush();
                        firstTab[0] = true;
                        return true;
                    }
                }
            }
            Set<String> matchSet = new java.util.TreeSet<>();
            for (String builtin : List.of("echo", "exit", "type", "pwd", "cd")) {
                if (builtin.startsWith(prefix)) {
                    matchSet.add(builtin);
                }
            }
            String path = System.getenv("PATH");
            for (String dir : path.split(File.pathSeparator)) {
                File folder = new File(dir);
                File[] files = folder.listFiles();
                if (files == null) {
                    continue;
                }
                for (File f : files) {
                    if (f.isFile()
                            && f.canExecute()
                            && f.getName().startsWith(prefix)) {
                        matchSet.add(f.getName());
                    }
                }
            }
            if (matchSet.size() == 1) {
                firstTab[0] = true;
                return original.apply();
            }
            if (matchSet.isEmpty()) {
                firstTab[0] = true;
                reader.getTerminal().puts(InfoCmp.Capability.bell);
                reader.getTerminal().flush();
                return true;
            }
            String commonPrefix = longestCommonPrefix(matchSet);
            if (commonPrefix.length() > prefix.length()) {
                String addition = commonPrefix.substring(prefix.length());
                reader.getBuffer().write(addition);
                reader.getTerminal().flush();
                firstTab[0] = true;
                return true;
            }
            if (firstTab[0]) {
                firstTab[0] = false;
                reader.getTerminal().puts(InfoCmp.Capability.bell);
                reader.getTerminal().flush();
                return true;
            }
            firstTab[0] = true;
            reader.getTerminal().writer().println();
            reader.getTerminal().writer().println(String.join("  ", matchSet));
            reader.getTerminal().writer().print("$ " + prefix);
            reader.getTerminal().writer().flush();
            return true;
        };
        reader.getWidgets().put("complete-word", myTab);
        reader.getKeyMaps().get(LineReader.MAIN).bind(new Reference("complete-word"), "\t");
        String path = System.getenv("PATH");
        String[] pathdirs = path.split(File.pathSeparator);
        File currentDirectory = new File(System.getProperty("user.dir"));

        while (true) {
            String input = reader.readLine("$ ");
            List<String> parts = tokenize(input);
            String redirectOp = null;
            String redirectFile = null;
            boolean syntaxError = false;

            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i).equals(">") || parts.get(i).equals("1>")) {
                    if (i + 1 >= parts.size()) {
                        System.out.println("syntax error: expected filename after >");
                        syntaxError = true;
                        break;
                    }
                    redirectOp = parts.get(i);
                    redirectFile = parts.get(i + 1);
                    parts.remove(i + 1);
                    parts.remove(i);
                    break;
                }else if (parts.get(i).equals("2>")) {
                    if (i + 1 >= parts.size()) {
                        System.out.println("syntax error: expected filename after 2>");
                        syntaxError = true;
                        break;
                    }
                    redirectOp = parts.get(i);
                    redirectFile = parts.get(i + 1);
                    parts.remove(i + 1);
                    parts.remove(i);
                    break;
                }else if (parts.get(i).equals(">>")) {
                    if (i + 1 >= parts.size()) {
                        System.out.println("syntax error: expected filename after >>");
                        syntaxError = true;
                        break;
                    }
                    redirectOp = parts.get(i);
                    redirectFile = parts.get(i + 1);
                    parts.remove(i + 1);
                    parts.remove(i);
                    break;
                }else if (parts.get(i).equals("1>>")) {
                    if (i + 1 >= parts.size()) {
                        System.out.println("syntax error: expected filename after 1>>");
                        syntaxError = true;
                        break;
                    }
                    redirectOp = parts.get(i);
                    redirectFile = parts.get(i + 1);
                    parts.remove(i + 1);
                    parts.remove(i);
                    break;
                }else if (parts.get(i).equals("2>>")) {
                    if (i + 1 >= parts.size()) {
                        System.out.println("syntax error: expected filename after 2>>");
                        syntaxError = true;
                        break;
                    }
                    redirectOp = parts.get(i);
                    redirectFile = parts.get(i + 1);
                    parts.remove(i + 1);
                    parts.remove(i);
                    break;
                }
            }
            if(syntaxError)
                continue;
            if (parts.isEmpty()) {
                continue;
            }
            String command = parts.get(0);
            if (command.equals("echo")) {
                if(redirectOp != null && (redirectOp.equals(">") || redirectOp.equals("1>"))){
                    File file = new File(redirectFile);
                    if (!file.isAbsolute()) {
                        file = new File(currentDirectory, redirectFile);
                    }
                    if(file.exists() && !file.canWrite()){
                        System.out.println("Permission denied: "+redirectFile);
                    }else{
                        file.createNewFile();
                        try (FileWriter writer = new FileWriter(file)) {
                            String output = parts.size() > 1 ? String.join(" ", parts.subList(1, parts.size())): "";
                            writer.write(output + System.lineSeparator());
                        }
                    }
                }else if (redirectOp != null &&(redirectOp.equals(">>") || redirectOp.equals("1>>"))){
                    File file = new File(redirectFile);
                    if(!file.isAbsolute()){
                        file = new File(currentDirectory, redirectFile);
                    }
                    if(file.exists() && !file.canWrite()){
                        System.out.println("Permission denied: "+redirectFile);
                    }else{
                        file.createNewFile();
                        try(FileWriter writer = new FileWriter(file, true)){
                            String output = parts.size() > 1 ? String.join(" ", parts.subList(1, parts.size())): "";
                            writer.write(output + System.lineSeparator());
                        }
                    }
                }else if (redirectOp != null && redirectOp.equals("2>")) {
                    File file = new File(redirectFile);
                    if (!file.isAbsolute()) {
                        file = new File(currentDirectory, redirectFile);
                    }
                    if(file.exists() && !file.canWrite()){
                        System.out.println("Permission denied: "+redirectFile);
                    }else{
                        file.createNewFile();
                        new FileWriter(file).close();  
                        String output = parts.size() > 1 ? String.join(" ", parts.subList(1, parts.size())): "";
                        System.out.println(output);
                    }
                }else if (redirectOp != null && redirectOp.equals("2>>")) {
                    File file = new File(redirectFile);
                    if(!file.isAbsolute()){
                        file = new File(currentDirectory, redirectFile);
                    }
                    if(file.exists() && !file.canWrite()){
                        System.out.println("Permission denied: "+redirectFile);
                    }else{
                        file.createNewFile();
                        new FileWriter(file, true).close();
                        String output = parts.size() > 1 ? String.join(" ", parts.subList(1, parts.size())): "";
                        System.out.println(output);
                        }
                    }else {
                    String output = parts.size() > 1 ? String.join(" ", parts.subList(1, parts.size())): "";
                    System.out.println(output);
                }
            } else if (command.equals("type")) {
                command = parts.get(1);
                if (command.equals("echo") || command.equals("exit") || command.equals("type") || command.equals("pwd")) {
                    System.out.println(command + " is a shell builtin");
                } else {
                    boolean found = false;
                    for (String dir : pathdirs) {
                        File file = new File(dir, command);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(command + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(command + ": not found");
                    }
                }
            } else if (command.equals("pwd")) {
                System.out.println(currentDirectory.getAbsolutePath());
            } else if (command.equals("cd")) {
                String destination = parts.get(1);
                String home = System.getenv("HOME");
                if (destination.equals("~")) {
                    destination = home;
                } else if (destination.startsWith("~")) {
                    destination = home + destination.substring(1);
                }
                File newDirectory = new File(destination);
                if (!newDirectory.isAbsolute()) {
                    newDirectory = new File(currentDirectory, destination);
                }
                if (newDirectory.exists() && newDirectory.isDirectory()) {
                    currentDirectory = newDirectory.getCanonicalFile();
                } else {
                    System.out.println("cd: " + destination + ": No such file or directory");
                }
            } else if (command.equals("exit")) {
                break;
            } else {
                boolean found = false;
                for (String dir : pathdirs) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        ProcessBuilder pb = new ProcessBuilder(parts);
                        if(redirectOp != null && (redirectOp.equals(">") || redirectOp.equals("1>"))){
                            File logFile = new File(redirectFile);
                            if (!logFile.isAbsolute()) {
                                logFile = new File(currentDirectory, redirectFile);
                            }
                            pb.redirectOutput(logFile); 
                            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        }else if(redirectOp != null && (redirectOp.equals(">>") || redirectOp.equals("1>>"))){
                            File logFile = new File(redirectFile);
                            if (!logFile.isAbsolute()) {
                                logFile = new File(currentDirectory, redirectFile);
                            }
                            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile)); 
                            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        }else if (redirectOp != null && (redirectOp.equals("2>") || redirectOp.equals("2>>"))) {
                            File logFile = new File(redirectFile);
                            if (!logFile.isAbsolute()) {
                                logFile = new File(currentDirectory, redirectFile);
                            }
                            if ("2>".equals(redirectOp)) {
                                    pb.redirectError(logFile);
                            }else if ("2>>".equals(redirectOp)) {
                                    pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
                            }
                            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                        }else{
                            pb.inheritIO();
                        }
                        Process process = pb.start();
                        process.waitFor();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println(command + ": not found");
                }
            }
        }
    }
}