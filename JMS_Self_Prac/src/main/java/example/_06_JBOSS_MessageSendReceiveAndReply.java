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
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class _06_JBOSS_MessageSendReceiveAndReply {

	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String REPLY_QUEUE_DESTINATION = "jms/queue/testReplyqueue";
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

		namingContext = new InitialContext(env);
		ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
		Destination destination = (Destination) namingContext.lookup(QUEUE_DESTINATION);
		final Destination replyDestination = (Destination) namingContext.lookup(REPLY_QUEUE_DESTINATION);
		context = connectionFactory.createContext("guest", "guest");

		// Create message and set JMSReplyTo to replyQueue。
		JMSProducer producer = context.createProducer();
		producer.setJMSReplyTo(replyDestination);
		producer.send(destination, "JACK ");

		// create consumer
		JMSConsumer consumer = context.createConsumer(destination);
		// create new Producer to send reply message
		final JMSProducer producer2 = context.createProducer();
		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					String text = ((TextMessage) m).getText();
					producer2.send(replyDestination, "Hello " + text);
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMSConsumer consumer2 = context.createConsumer(replyDestination);
		consumer2.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println(((TextMessage) m).getText());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		
		if (namingContext != null) {
			namingContext.close();
		}
		if (context != null) {
			context.close();
		}

	}

}
