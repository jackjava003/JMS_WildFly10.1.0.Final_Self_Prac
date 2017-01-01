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
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class _01_JBOSS_MessageSendAndReceive {

	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	public static void main(String[] args) throws JMSException, NamingException {

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
			JMSConsumer consumer= context.createConsumer(destination);
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
				Message recvMessage = consumer.receiveNoWait();
				System.out.println("Message Recieved: " + ((TextMessage) recvMessage).getText());
				recvMessage.clearBody();
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
