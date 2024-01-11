import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Date;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assn3 {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        double ptime = 0;
        CommandHistory commandInput = new CommandHistory();

        while (true) {
            String path = System.getProperty("user.dir");
            System.out.print("[" + path +"]: ");
            String input = scan.nextLine();
            String[] commandArgs = splitCommand(input);
            //TODO Multiple Arguments
            switch (commandArgs[0]) {
                case "ptime":
                    System.out.printf("Total time in child processes: %.4f seconds%n", ptime);
                    break;
                case "list":
                    list(path);
                    break;
                case "cd":
                    if (commandArgs.length > 1) {
                        changeDirectory(commandArgs[1]);
                    } else {
                        changeDirectory("");
                    }
                    break;
                case "mdir":
                    if (commandArgs.length > 1) {
                        createDir(commandArgs[1]);
                    }
                    break;
                case "rdir":
                    if (commandArgs.length > 1) {
                        deleteDir(commandArgs[1]);
                    }
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    ptime += external(commandArgs) / 1000;
            }
        }
    }

    private static void deleteDir(String dirName) {
        Path path = Paths.get(System.getProperty("user.dir"), dirName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Error: Directory doesn't exits");
        }
    }

    private static void createDir(String dirName) {
        Path path = Paths.get(System.getProperty("user.dir"), dirName);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void list(String pathString) {
        Path path = Paths.get(pathString);
        try {
            for (Path value : Files.newDirectoryStream(path)) {
                printPathValue(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printPathValue(Path path) {
        try {
            //TODO Permissions
            String size = String.format("%10d", Files.size(path));
            String lastModified = new SimpleDateFormat("MMM dd HH:mm").format(Files.getLastModifiedTime(path).toMillis());
            System.out.printf("%s %s %s%n", size, lastModified, path.getFileName().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void changeDirectory(String args) {
        String currentDirectory = System.getProperty("user.dir");
        if (Objects.equals(args, "")) {
            String home = System.getProperty("user.home");
            System.setProperty("user.dir", home);
        } else if (Objects.equals(args, "..")) {
            String parent = new File(currentDirectory).getParent();
            if (parent != null) {
                System.setProperty("user.dir", parent);
            }
        } else {
            File dir = new File(currentDirectory, args);
            if (dir.isDirectory() && dir.exists()) {
                System.setProperty("user.dir", dir.getAbsolutePath());
            }
        }
    }

    private static double external(String[] command) {
        try {
            long start = System.currentTimeMillis();

            //TODO
            boolean background = command[0].trim().endsWith("&");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(System.getProperty("user.dir")));

            Process process = processBuilder.start();

            readOutput(process.getInputStream());
            readOutput(process.getErrorStream());

            if (!background) {
                process.waitFor();
            }

            long end = System.currentTimeMillis();
            return (end - start);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Split the user command by spaces, but preserving them when inside double-quotes.
     * Code Adapted from: https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
     */
    private static String[] splitCommand(String command) {
        java.util.List<String> matchList = new java.util.ArrayList<>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(command);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList.toArray(new String[matchList.size()]);
    }
}
