package example;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class _08_JMSCorrelationIDTest {
	private Queue queue;
	private Session session;

	public _08_JMSCorrelationIDTest() throws JMSException, InterruptedException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		Connection connection = factory.createConnection();
		connection.start();
		queue = new ActiveMQQueue("testQueue");
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		setupConsumer("ConsumerA");
		setupConsumer("ConsumerB");
		setupConsumer("ConsumerC");
		setupProducer("ProducerA", "ConsumerA");
		setupProducer("ProducerB", "ConsumerB");
		setupProducer("ProducerC", "ConsumerC");
		Thread th = Thread.currentThread();
		th.sleep(500);
		connection.close();
	}

	private void setupConsumer(final String name) throws JMSException {
		// Create consumer, only receive the message that belong to itself
		MessageConsumer consumer = session.createConsumer(queue, "receiver='" + name + "'");
		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					MessageProducer producer = session.createProducer(queue);
					System.out.println(name + " get:" + ((TextMessage) m).getText());
					// reply a message
					Message replyMessage = session.createTextMessage("Reply from " + name);
					// Setting JMSCorrelationID from received message
					replyMessage.setJMSCorrelationID(m.getJMSMessageID());
					producer.send(replyMessage);
				} catch (JMSException e) {
				}
			}
		});
	}

	private void setupProducer(final String name, String consumerName) throws JMSException {
		MessageProducer producer = session.createProducer(queue);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		// Create a message and setting a receiver, naming consumer's name
		Message message = session.createTextMessage("Message from " + name);
		message.setStringProperty("receiver", consumerName);
		producer.send(message);
		// Wait for reply message
		MessageConsumer replyConsumer = session.createConsumer(queue,
				"JMSCorrelationID='" + message.getJMSMessageID() + "'");
		replyConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println(name + " get reply:" + ((TextMessage) m).getText());
				} catch (JMSException e) {
				}
			}
		});
	}

	public static void main(String[] args) throws Exception {
		_08_JMSCorrelationIDTest test = new _08_JMSCorrelationIDTest();
	}
}
