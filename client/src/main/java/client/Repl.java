package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {

    private enum State { PRELOGIN, POSTLOGIN, GAMEPLAY }

    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private final GameplayClient gameplayClient;
    private final Scanner scanner = new Scanner(System.in);

    private State state = State.PRELOGIN;
    private boolean running = true;

    public Repl(String serverUrl) {
        ServerFacade facade = new ServerFacade(serverUrl);
        gameplayClient = new GameplayClient(serverUrl, this);
        preloginClient = new PreloginClient(facade, this);
        postloginClient = new PostloginClient(facade, this, gameplayClient);
    }

    public void run() {
        System.out.println("♕ 240 Chess Client. Type 'help' to get started.");

        while (running) {
            printPrompt();

            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            try {
                String result = switch (state) {
                    case PRELOGIN -> preloginClient.eval(input);
                    case POSTLOGIN -> postloginClient.eval(input);
                    case GAMEPLAY -> gameplayClient.eval(input);
                };
                if (result != null && !result.isEmpty()) {
                    System.out.println(result);
                }
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    /** Prints the prompt for the current state. Also called after asynchronous output. */
    void printPrompt() {
        String label = switch (state) {
            case PRELOGIN -> "[LOGGED OUT] ";
            case POSTLOGIN -> "[LOGGED IN] ";
            case GAMEPLAY -> "[IN GAME] ";
        };
        System.out.print(RESET_TEXT_COLOR + label + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    Scanner scanner() {
        return scanner;
    }

    void setLoggedIn(boolean loggedIn) {
        state = loggedIn ? State.POSTLOGIN : State.PRELOGIN;
    }

    void enterGameplay() {
        state = State.GAMEPLAY;
    }

    void leaveGameplay() {
        state = State.POSTLOGIN;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

}
