package com.ibm.hybrid.cloud.sample.stocktrader.notification;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.transaction.Transactional;

import org.eclipse.microprofile.opentracing.Traced;


@ApplicationScoped
public class JMSHelper {
	private static Logger logger = Logger.getLogger(JMSHelper.class.getName());
	
	@Inject
	@JMSConnectionFactory("jms/Portfolio/NotificationQueueConnectionFactory")
	private JMSContext jmsContext;
	
	@Resource(lookup = "jms/Portfolio/NotificationQueue")
	private Queue queue;
	

	@Traced
	@Transactional
	void invokeJMS(Object json) throws JMSException, NamingException {

		String contents = json.toString();
		logger.info("Sending "+contents+" to "+queue.getQueueName());

		jmsContext.createProducer().send(queue, contents);

		logger.info("JMS Message sent successfully!");
	}
	
	
}
