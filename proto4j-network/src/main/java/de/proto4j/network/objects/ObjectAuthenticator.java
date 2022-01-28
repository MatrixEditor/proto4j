package de.proto4j.network.objects;//@date 28.01.2022

public interface ObjectAuthenticator {

    ContextResult authenticate(ObjectExchange exchange);

    abstract class ContextResult {}

    class Failure extends ContextResult {

        private final int code;

        public Failure(int code) {this.code = code;}

        public int getCode() {
            return code;
        }
    }

    final class Success extends ContextResult {
        private final ObjectPrincipal principal;

        public Success(ObjectPrincipal principal) {this.principal = principal;}

        public ObjectPrincipal getPrincipal() {
            return principal;
        }
    }

    final class Retry extends Failure {

        public Retry(int code) {
            super(code);
        }
    }
}
