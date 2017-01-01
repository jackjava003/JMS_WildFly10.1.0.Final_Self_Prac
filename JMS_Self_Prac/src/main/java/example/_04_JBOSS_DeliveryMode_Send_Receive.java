package example;

import java.util.Properties;
import java.util.Scanner;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class _04_JBOSS_DeliveryMode_Send_Receive {

	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	public static void main(String[] args) throws NamingException, InterruptedException {
		Context namingContext = null;
		JMSContext context = null;
		final Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
		env.put(Context.SECURITY_PRINCIPAL, "guest");
		env.put(Context.SECURITY_CREDENTIALS, "guest");
		try {
			namingContext = new InitialContext(env);
			ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
			Destination destination = (Destination) namingContext.lookup(QUEUE_DESTINATION);
			context = connectionFactory.createContext("guest", "guest");

			JMSProducer producer = context.createProducer();
			Scanner sc = new Scanner(System.in);
			while (true) {
				if (sc.hasNext()) {
					String input;
					input = sc.nextLine();
					producer.setDeliveryMode(DeliveryMode.PERSISTENT);
					producer.send(destination, "A persistent Message " + input);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					producer.send(destination, "A non persistent Message " + input);
					System.out.println("Send Message Completed!");
					// producer disconnect
					if (namingContext != null) {
						namingContext.close();
					}
					if (context != null) {
						context.close();
					}
					System.out.println("producer disconnect");
					break;
				}

			}
			Thread th = Thread.currentThread();
			th.sleep(1000L);
			System.out.println("Thread wake");
			// consumer connect to server
			namingContext = new InitialContext(env);
			connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
			destination = (Destination) namingContext.lookup(QUEUE_DESTINATION);
			context = connectionFactory.createContext("guest", "guest");

			JMSConsumer consumer = context.createConsumer(destination);
			consumer.setMessageListener(new MessageListener() {
				public void onMessage(Message m) {
					try {
						System.out.println("Consumer get " + ((TextMessage) m).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
		} finally {
			if (namingContext != null) {
				namingContext.close();
			}
			if (context != null) {
				context.close();
			}
		}

	}

}
