package example;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class _09_TemporaryQueueTest {

	public static void main(String[] args) throws JMSException, InterruptedException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		Connection connection = factory.createConnection();
		connection.start();
		Queue queue = new ActiveMQQueue("testQueue2");
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// use session to create a TemporaryQueue。
		TemporaryQueue replyQueue = session.createTemporaryQueue();
		// received message and reply to certain queue(replyQueue)
		MessageConsumer comsumer = session.createConsumer(queue);
		comsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("Get Message: " + ((TextMessage) m).getText());
					MessageProducer producer = session.createProducer(m.getJMSReplyTo());
					producer.send(session.createTextMessage("ReplyMessage"));
				} catch (JMSException e) {
				}
			}
		});
		// use same connection to create an other session, to read the message from replyQueue
		Session session2 = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer replyComsumer = session2.createConsumer(replyQueue);
		replyComsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("Get reply: " + ((TextMessage) m).getText());
				} catch (JMSException e) {
				}
			}
		});
		MessageProducer producer = session.createProducer(queue);
		TextMessage message = session.createTextMessage("SimpleMessage");
		message.setJMSReplyTo(replyQueue);
		producer.send(message);
		Thread.currentThread().sleep(500);
		connection.close();
	}
}
