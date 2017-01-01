package example;

import java.util.Properties;
import java.util.Scanner;

import javax.jms.ConnectionFactory;
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

public class _02_JBOSS_QuenueTest {

	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	public static void main(String[] args) throws NamingException, JMSException {
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
			JMSConsumer comsumer1 = context.createConsumer(destination);
			comsumer1.setMessageListener(new MessageListener() {
				public void onMessage(Message m) {
					try {
						System.out.println("Consumer1 get " + ((TextMessage) m).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
			JMSConsumer comsumer2 = context.createConsumer(destination);
			comsumer2.setMessageListener(new MessageListener() {
				public void onMessage(Message m) {
					try {
						System.out.println("Consumer2 get " + ((TextMessage) m).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
			JMSProducer producer = context.createProducer();
			Scanner sc = new Scanner(System.in);
			while (true) {
				if (sc.hasNext()) {
					String input;
					if (!(input = sc.nextLine()).equalsIgnoreCase("stop")) {
						producer.send(destination, input);
						System.out.println("Send Message Completed!");
					} else {
						break;
					}
				}
			}
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
