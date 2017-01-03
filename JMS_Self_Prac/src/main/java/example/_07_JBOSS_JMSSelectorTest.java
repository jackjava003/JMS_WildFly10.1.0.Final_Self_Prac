package example;

import java.util.Properties;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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

public class _07_JBOSS_JMSSelectorTest {

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

		namingContext = new InitialContext(env);
		ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
		Destination destination = (Destination) namingContext.lookup(QUEUE_DESTINATION);
		context = connectionFactory.createContext("guest", "guest");

		JMSConsumer consumerA = context.createConsumer(destination, "receiver = 'A'");
		consumerA.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("ConsumerA get " + ((TextMessage) m).getText());
				} catch (JMSException e1) {
				}
			}
		});

		JMSConsumer consumerB = context.createConsumer(destination, "receiver = 'B'");
		consumerB.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println("ConsumerB get " + ((TextMessage) m).getText());
				} catch (JMSException e1) {
				}
			}
		});

		JMSProducer producer = context.createProducer();
		for (int i = 0; i < 10; i++) {
			String receiver = (i % 3 == 0 ? "A" : "B");
			producer.setProperty("receiver", receiver);
			producer.send(destination, "Message" + i + ", receiver:" + receiver);
			Thread.currentThread().sleep(500);
		}
		
		if (namingContext != null) {
			namingContext.close();
		}
		if (context != null) {
			context.close();
		}
	}

}
