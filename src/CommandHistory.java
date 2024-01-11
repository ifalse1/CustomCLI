import java.util.ArrayList;
import java.util.Scanner;

public class CommandHistory {

    private Scanner scanner;

    private ArrayList<String> history;

    public CommandHistory() {
        this.scanner = new Scanner(System.in);
    }

    public String readCommand() {
        return scanner.nextLine();
    }

}
