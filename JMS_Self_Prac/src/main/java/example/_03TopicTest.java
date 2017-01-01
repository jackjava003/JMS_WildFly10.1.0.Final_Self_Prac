package example;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

public class _03TopicTest {

	public static void main(String[] args) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		Connection connection = factory.createConnection();
		connection.start();
		// Create a Topic
		Topic topic = new ActiveMQTopic("testTopic");
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// consumer1 
		MessageConsumer consumer1 = session.createConsumer(topic);
		consumer1.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("Consumer1 get " + ((TextMessage) m).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		// consumer2 
		MessageConsumer consumer2 = session.createConsumer(topic);
		consumer2.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("Consumer2 get " + ((TextMessage) m).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		// create producer and send message
		MessageProducer producer = session.createProducer(topic);
		for (int i = 0; i < 10; i++) {
			producer.send(session.createTextMessage("Message:" + i));
		}
		connection.close();
	}

}
