package example;

import java.util.Properties;

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

public class _08_JBOSS_JMSCorrelationIDTest {

	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	Context namingContext = null;
	JMSContext context = null;
	Destination destination = null;

	public _08_JBOSS_JMSCorrelationIDTest() throws JMSException, NamingException, InterruptedException {

		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
		env.put(Context.SECURITY_PRINCIPAL, "guest");
		env.put(Context.SECURITY_CREDENTIALS, "guest");

		namingContext = new InitialContext(env);
		ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
		destination = (Destination) namingContext.lookup(QUEUE_DESTINATION);
		context = connectionFactory.createContext("guest", "guest");
		
		Thread th = Thread.currentThread();
		setupConsumer("Consumer A");
		th.sleep(500);
		setupConsumer("Consumer B");
		th.sleep(500);
		setupConsumer("Consumer C");
		th.sleep(500);
		setupProducer("Producer A", "Consumer A");
		th.sleep(500);
		setupProducer("Producer B", "Consumer B");
		th.sleep(500);
		setupProducer("Producer C", "Consumer C");
		context.close();
	}

	private void setupConsumer(final String name) throws JMSException {
		// Create consumer, only receive the message that belong to itself
		JMSConsumer consumer = context.createConsumer(destination, "receiver='" + name + "'");

		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					JMSProducer producer = context.createProducer();
					System.out.println(name + " get:" + ((TextMessage) m).getText());
					// Setting JMSCorrelationID from received message
					producer.setJMSCorrelationID(m.getJMSCorrelationID());
					producer.send(destination, "Reply from " + name);
				} catch (JMSException e) {
				}
			}
		});
	}

	private void setupProducer(final String name, String consumerName) throws JMSException {
		JMSProducer producer = context.createProducer();
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		// Create a message and setting a receiver, naming consumer's name
		producer.setProperty("receiver", consumerName);
		producer.setJMSCorrelationID(name+consumerName);
		producer.send(destination, "Message from " + name);
		// Wait for reply message
		JMSConsumer replyConsumer = context.createConsumer(destination,
				"JMSCorrelationID='" + producer.getJMSCorrelationID() + "'");
		replyConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					System.out.println(name + " get reply:" + ((TextMessage) m).getText());
				} catch (JMSException e) {
				}
			}
		});
	}

	public static void main(String[] args) throws NamingException, JMSException, InterruptedException {
		new _08_JBOSS_JMSCorrelationIDTest();
	}

}
