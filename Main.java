import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Implement this yourself
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
            }else if(c == '\"' && !inSingleQuotes){
                inDoubleQuotes = !inDoubleQuotes;
            }else if(c == '\\' && i+1 < input.length()){
                char next = input.charAt(i+1);
                if(inSingleQuotes){
                    current.append(c);
                }else if(inDoubleQuotes){
                    if(next == '"' || next == '\\'){
                        current.append(next);
                        i++;
                    }else{
                        current.append(c);
                    }
                }
                else{
                    current.append(next);
                    i++;
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

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String path = System.getenv("PATH");
        String[] pathdirs = path.split(File.pathSeparator);
        File currentDirectory = new File(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            List<String> parts = tokenize(input);
            if (parts.isEmpty()) {
                continue;
            }
            String command = parts.get(0);
            if (command.equals("echo")) {
                if (parts.size() > 1) {
                    System.out.println(String.join(" ", parts.subList(1, parts.size())));
                } else {
                    System.out.println();
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
                        pb.inheritIO();
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