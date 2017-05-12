Feature: A greeting and secret message is sent to the customer.
  Scenario: Received messages are appended a secret message and sent to the client.
    Given a 'Hello Customer!' message is sent
    When the message is retrieved by the service
    Then 'Hello Customer! This is a secret message!' message is delivered to the customer service
  