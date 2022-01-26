package de.proto4j.network.http;//@date 25.01.2022

enum ErrorMessage {
    HTTP_ON_HTTPS("WARNING! : Trying to start a Https-Server on Http! [%s]\n"),
    HTTP_NOT_DEFINED("WARNING! : Http not defined for Http-Server! [%s]\n"),
    EXECUTION_ERROR("ERROR! : Method invocation failed! [%s] (%s)")
    ;

    private final String message;

    ErrorMessage(String message) {this.message = message;}

    public String getMessage() {
        return message;
    }

}
