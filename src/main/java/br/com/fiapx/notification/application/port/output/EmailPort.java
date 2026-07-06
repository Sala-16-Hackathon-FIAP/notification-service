package br.com.fiapx.notification.application.port.output;

public interface EmailPort {
    void send(String to, String subject, String body);
}
