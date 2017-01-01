package example;

import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class _01MessageSendAndReceive {

	public static void main(String[] args) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		Connection connection = factory.createConnection();
		connection.start();
		Queue queue = new ActiveMQQueue("testQueue");
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Scanner sc = new Scanner(System.in);
		MessageProducer producer = session.createProducer(queue);
		MessageConsumer consumer = session.createConsumer(queue);
		while (true) {

			if (sc.hasNext()) {
				String input;
				if (!(input = sc.nextLine()).equalsIgnoreCase("stop")) {
					Message message = session.createTextMessage(input);
					producer.send(message);
					System.out.println("Send Message Completed!");
				} else {
					break;
				}
			}
			Message recvMessage = consumer.receive();
			System.out.println("Message Recieved: " + ((TextMessage) recvMessage).getText());
			recvMessage.clearBody();
		}
		sc.close();
		connection.close();
		
	}

}
