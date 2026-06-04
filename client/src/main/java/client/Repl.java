package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    
    private final PreloginClient preloginClient;
    private final PostloginClient postliginClient;
    private final Scanner scanner = new Scanner(System.in);

    private boolean loggedIn = false;
    private boolean running = true;

    public Repl(String serverUrl) {
        ServerFacade facade = new ServerFacade(serverUrl);
        preloginClient = new PreloginClient(facade, this)
    }

    public void run() {
        System.out.println("♕ 240 Chess Client. Type 'help' to get started.");

        while (running) {
            String prompt = loggedIn ? "[LOGGED IN] >>> " : "[LOGGED OUT] >>> ";
            System.out.print(prompt);

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            try {
                String result;
                if (loggedIn) {
                    result = postloginClient.eval(input);
                } else {
                    result = preloginClient.eval(input);
                }
                System.out.println(result);
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    void setRunning(boolean running) {
        this.running = running;
    }
    
}
