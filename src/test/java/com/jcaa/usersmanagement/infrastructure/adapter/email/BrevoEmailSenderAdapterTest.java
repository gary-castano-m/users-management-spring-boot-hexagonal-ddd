package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for BrevoEmailSenderAdapter.
 *
 * <p>HttpClient is mocked: no real HTTP request reaches Brevo, so no emails are sent and the
 * daily quota is not consumed. Covers the successful dispatch, the request built for the Brevo
 * API, HTTP error responses, network failures, thread interruption and missing configuration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BrevoEmailSenderAdapter")
class BrevoEmailSenderAdapterTest {

  private static final String API_KEY = "xkeysib-test-key";
  private static final String API_URL = "https://api.brevo.test/v3/smtp/email";
  private static final String SENDER_EMAIL = "noreply@example.com";
  private static final String SENDER_NAME = "Oye! Test";
  private static final String DEST_EMAIL = "john@example.com";
  private static final String DEST_NAME = "John Doe";
  private static final String SUBJECT = "Account created";
  private static final String BODY = "<html>Welcome</html>";

  @Mock private HttpClient httpClient;
  @Mock private HttpResponse<String> httpResponse;

  private BrevoEmailSenderAdapter adapter;
  private EmailDestinationModel destination;

  @BeforeEach
  void setUp() {
    final BrevoConfig config = new BrevoConfig(API_KEY, API_URL, SENDER_EMAIL, SENDER_NAME);
    adapter = new BrevoEmailSenderAdapter(config, httpClient);
    destination = new EmailDestinationModel(DEST_EMAIL, DEST_NAME, SUBJECT, BODY);
  }

  @AfterEach
  void clearInterruptFlag() {
    // Clears the interrupt flag in case a test left it set
    Thread.interrupted();
  }

  // ── send() — happy path

  @Test
  @DisplayName("send() completes without errors when Brevo responds 201")
  void shouldSendEmailWhenBrevoRespondsCreated() throws Exception {
    // Arrange
    when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);

    // Act & Assert
    assertDoesNotThrow(() -> adapter.send(destination));
    verify(httpClient).send(any(HttpRequest.class), any());
  }

  // ── send() — request built for the Brevo API

  @Test
  @DisplayName("send() posts the expected JSON with the api-key header to the configured URL")
  void shouldBuildRequestWithApiKeyAndExpectedJson() throws Exception {
    // Arrange
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    when(httpClient.<String>send(requestCaptor.capture(), any())).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);

    // Act
    adapter.send(destination);

    // Assert
    final HttpRequest request = requestCaptor.getValue();
    final JsonNode json = new ObjectMapper().readTree(bodyOf(request));
    assertAll(
        "request must match the Brevo API contract",
        () -> assertEquals("POST", request.method()),
        () -> assertEquals(URI.create(API_URL), request.uri()),
        () -> assertEquals(API_KEY, request.headers().firstValue("api-key").orElseThrow()),
        () -> assertEquals(SENDER_EMAIL, json.at("/sender/email").asText()),
        () -> assertEquals(SENDER_NAME, json.at("/sender/name").asText()),
        () -> assertEquals(DEST_EMAIL, json.at("/to/0/email").asText()),
        () -> assertEquals(DEST_NAME, json.at("/to/0/name").asText()),
        () -> assertEquals(SUBJECT, json.at("/subject").asText()),
        () -> assertEquals(BODY, json.at("/htmlContent").asText()));
  }

  // ── send() — HTTP error response → EmailSenderException

  @Test
  @DisplayName("send() throws EmailSenderException with status and body when Brevo rejects")
  void shouldThrowEmailSenderExceptionWhenBrevoRespondsError() throws Exception {
    // Arrange
    when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(401);
    when(httpResponse.body()).thenReturn("{\"message\":\"Key not found\"}");

    // Act
    final EmailSenderException exception =
        assertThrows(EmailSenderException.class, () -> adapter.send(destination));

    // Assert
    final String detail = exception.getCause().getMessage();
    assertAll(
        "exception detail must explain the Brevo rejection",
        () -> assertTrue(detail.contains("401"), "must include the HTTP status"),
        () -> assertTrue(detail.contains("Key not found"), "must include the Brevo response"),
        () -> assertTrue(detail.contains(DEST_EMAIL), "must identify the recipient"));
  }

  // ── send() — network failure → EmailSenderException

  @Test
  @DisplayName("send() wraps IOException into EmailSenderException")
  void shouldThrowEmailSenderExceptionWhenNetworkFails() throws Exception {
    // Arrange
    when(httpClient.<String>send(any(HttpRequest.class), any()))
        .thenThrow(new IOException("Connection reset"));

    // Act
    final EmailSenderException exception =
        assertThrows(EmailSenderException.class, () -> adapter.send(destination));

    // Assert
    assertTrue(exception.getCause().getMessage().contains("Connection reset"));
  }

  // ── send() — interruption → EmailSenderException and interrupt flag restored

  @Test
  @DisplayName("send() restores the interrupt flag when the thread is interrupted")
  void shouldRestoreInterruptFlagWhenInterrupted() throws Exception {
    // Arrange
    when(httpClient.<String>send(any(HttpRequest.class), any()))
        .thenThrow(new InterruptedException("interrupted"));

    // Act
    assertThrows(EmailSenderException.class, () -> adapter.send(destination));

    // Assert
    assertTrue(Thread.currentThread().isInterrupted(), "interrupt flag must be restored");
  }

  // ── send() — missing configuration → fails before calling Brevo

  @Test
  @DisplayName("send() fails fast without calling Brevo when the API key is missing")
  void shouldFailFastWhenApiKeyIsMissing() {
    // Arrange
    final BrevoConfig emptyKeyConfig = new BrevoConfig("", API_URL, SENDER_EMAIL, SENDER_NAME);
    final BrevoEmailSenderAdapter misconfiguredAdapter =
        new BrevoEmailSenderAdapter(emptyKeyConfig, httpClient);

    // Act & Assert
    assertThrows(EmailSenderException.class, () -> misconfiguredAdapter.send(destination));
    verifyNoInteractions(httpClient);
  }

  // ── Helper: reads the body of an HttpRequest
  // HttpRequest stores the body as a reactive publisher, not as text.
  // This subscriber collects the published chunks and joins them into a String.

  private static String bodyOf(final HttpRequest request) {
    final StringBuilder body = new StringBuilder();
    request
        .bodyPublisher()
        .orElseThrow()
        .subscribe(
            new Flow.Subscriber<ByteBuffer>() {
              @Override
              public void onSubscribe(final Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
              }

              @Override
              public void onNext(final ByteBuffer item) {
                body.append(StandardCharsets.UTF_8.decode(item));
              }

              @Override
              public void onError(final Throwable throwable) {
                throw new IllegalStateException(throwable);
              }

              @Override
              public void onComplete() {
                // Nothing to do: the body is complete
              }
            });
    return body.toString();
  }
}