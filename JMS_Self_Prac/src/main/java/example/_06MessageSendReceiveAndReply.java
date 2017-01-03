package example;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class _06MessageSendReceiveAndReply {

	public static void main(String[] args) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		Connection connection = factory.createConnection();
		connection.start();
		// Send message to this Queue
		Queue queue = new ActiveMQQueue("testQueue");

		// Reply message to this Queue
		Queue replyQueue = new ActiveMQQueue("replyQueue");
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create message and set JMSReplyTo to replyQueue。
		Message message = session.createTextMessage("JACK");
		message.setJMSReplyTo(replyQueue);
		MessageProducer producer = session.createProducer(queue);
		producer.send(message);
		// create consumer
		MessageConsumer consumer = session.createConsumer(queue);
		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					// create new MessageProducer to send reply message
					MessageProducer producer = session.createProducer(m.getJMSReplyTo());
					producer.send(session.createTextMessage("Hello " + ((TextMessage) m).getText()));
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
			}
		});
		// this consumer is used to receive the reply message
		MessageConsumer consumer2 = session.createConsumer(replyQueue);
		consumer2.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println(((TextMessage) m).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
