package client;

import model.AuthData;

public class PreloginClient {
    
    private final ServerFacade facade;
    private final Repl repl;

    public PreloginClient(ServerFacade facade, Repl repl) {
        this.facade = facade;
        this.repl = repl;
    }

    public String eval(String input) throws ResponseException {

        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> help();
            case "quit" -> quit();
            case "login" -> login(tokens);
            case "register" -> register(tokens);
            default -> "Unknown command. Type 'help' for available commands.";
        };

    }

    private String help() {
        return """
                  register <USERNAME> <PASSWORD> <EMAIL> - create an account
                  login <USERNAME> <PASSWORD> - log in to an existing account
                  quit - exit the program
                  help - show this message""";
    }

    private String quit() {
        repl.setRunning(false);
        return "Goodbye!";
    }

    private String login(String[] tokens) throws ResponseException {
        if (tokens.length != 3) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }

        AuthData auth = facade.login(tokens[1], tokens[2]);
        repl.setLoggedIn(true);
        // PostloginClient needs the auth token, so we pass it through
        PostloginClient.authToken = auth.authToken();
        return "Logged in as " + auth.username() + ".";
    }

    private String register(String[] tokens) throws ResponseException {
        if (tokens.length != 4) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }

        AuthData auth = facade.register(tokens[1], tokens[2], tokens[3]);
        repl.setLoggedIn(true);
        PostloginClient.authToken = auth.authToken();
        return "Registered and logged in as " + auth.username() + ".";
    }
    
}
