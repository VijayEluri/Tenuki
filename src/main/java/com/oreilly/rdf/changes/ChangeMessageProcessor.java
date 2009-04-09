package com.oreilly.rdf.changes;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.Model;

public class ChangeMessageProcessor implements Runnable {

	private Model model;
	private ConnectionFactory connectionFactory;
	private String topicName;

	public ChangeMessageProcessor(Model model, String topic,
			ConnectionFactory connectionFactory) {
		this.model = model;
		this.connectionFactory = connectionFactory;
		this.topicName = topic;
	}

	@Override
	public void run() {
		try {
			ChangesetHandler handler = new ChangesetHandler(model);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createTopic(topicName);
			MessageConsumer consumer = session.createConsumer(destination);
			boolean keepGoing = true;
			while (keepGoing){
				Message message = consumer.receive();
				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
					String xml = textMessage.getText();
					Changeset changeset = new InputStreamChangeset(IOUtils.toInputStream(xml));
					handler.applyChangeset(changeset);
				}
				if(Thread.interrupted()){
					keepGoing = false;
				}
			}
			consumer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
		}
	}

}