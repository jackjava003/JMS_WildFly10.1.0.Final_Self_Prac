package example;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class _09_JBOSS_TemporaryQueueTest {
	
	private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
	private static final String QUEUE_DESTINATION = "jms/queue/testqueue";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	public static void main(String[] args) throws JMSException, NamingException, InterruptedException {
		Context namingContext = null;
		
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
		env.put(Context.SECURITY_PRINCIPAL, "guest");
		env.put(Context.SECURITY_CREDENTIALS, "guest");

		namingContext = new InitialContext(env);
		ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
		Connection connection = connectionFactory.createConnection("guest", "guest");
		connection.start();
		
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("testQueue");
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
		Thread th = Thread.currentThread();
		th.sleep(1500);
		connection.close();
	}
}
