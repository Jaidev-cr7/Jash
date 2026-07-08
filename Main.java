import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String path =  System.getenv("PATH");
        String[] pathdirs = path.split(java.io.File.pathSeparator);
        File currentDirectory = new File(System.getProperty("user.dir"));

        while(true){
        System.out.print("$ ");
        String input = sc.nextLine();
        String[] parts = input.split(" ");
        String command = parts[0];
        if (command.equals("echo")) {
            System.out.println(input.substring(5));
        }
        else if(command.equals("type")){
            command = parts[1];
            if(command.equals("echo")|| command.equals("exit") || command.equals("type") || command.equals("pwd")){
                System.out.println(command +" is a shell builtin");
            }else{
                boolean found = false;
                for (int i = 0; i < pathdirs.length; i++) {
                    File file = new File(pathdirs[i], command);
                    if(file.exists() && file.canExecute()){
                        System.out.println(command + " is "+file.getAbsolutePath());
                        found = true;
                    }
                }
                if(!found){
                    System.out.println(command +": not found");
                }
                }
        }else if(command.equals("pwd")){
           System.out.println(currentDirectory.getAbsolutePath());
        }else if(command.equals("cd")){
            String destination = parts[1];
            String home = System.getenv("HOME");
            if(destination.equals("~")){
                destination = home;
            }else if(destination.startsWith("~")){
                destination = home + destination.substring(1);
            }
            File newDirectory = new File(destination);
            if (!newDirectory.isAbsolute()) {
                    newDirectory = new File(currentDirectory,destination);
            }
            if(newDirectory.exists() && newDirectory.isDirectory()){
                currentDirectory = newDirectory.getCanonicalFile();
            }else{
                    System.out.println("cd: "+destination+": No such file or directory");
            }
        }else if (command.equals("exit")) {
            break;
        }
        else{
            boolean found = false;
                for (int i = 0; i < pathdirs.length; i++) {
                    File file = new File(pathdirs[i], command);
                    if(file.exists() && file.canExecute()){
                        List<String> commandList = new ArrayList<>();
                        commandList.add(parts[0]);
                        for (int j = 1; j < parts.length; j++) {
                            commandList.add(parts[j]);
                        }
                        ProcessBuilder pb = new ProcessBuilder(commandList);
                        pb.inheritIO();
                        Process process = pb.start();
                        process.waitFor();
                        found = true;
                    }
                }
                if(!found){
                    System.out.println(command +": not found");
                }
            }
        
        }
    }
}