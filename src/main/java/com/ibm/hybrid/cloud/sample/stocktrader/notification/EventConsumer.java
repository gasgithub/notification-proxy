package com.ibm.hybrid.cloud.sample.stocktrader.notification;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import com.ibm.hybrid.cloud.sample.stocktrader.portfolio.event.BaseEvent;
import com.ibm.hybrid.cloud.sample.stocktrader.portfolio.event.LoyaltyChangeEvent;
import com.ibm.hybrid.cloud.sample.stocktrader.portfolio.event.StockPurchasedEvent;

@ApplicationScoped
public class EventConsumer {
	private static Logger logger = Logger.getLogger(EventConsumer.class.getName());
	private Jsonb jsonb = JsonbBuilder.create();
	
	@Inject
	private JMSHelper jmsHelper;

	@Incoming("stock-channel-inbound")
	@Outgoing("stock-channel-outbound")
	public PublisherBuilder<String> receive(String eventAsString) {
		logger.info("Received: " + eventAsString);
		BaseEvent event = null;
		try {
			event = jsonb.fromJson(eventAsString, BaseEvent.class);
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		String type = event.getType();
		
		if(type.equals(BaseEvent.TYPE_LOYALTY_CHANGED)) {
			// handle 
			LoyaltyChangeEvent loyaltyChangeEvent = jsonb.fromJson(eventAsString, LoyaltyChangeEvent.class);
			LoyaltyChange message = new LoyaltyChange(loyaltyChangeEvent.getOwner(), loyaltyChangeEvent.getOldLoyalty(), loyaltyChangeEvent.getNewLoyalty());
			try {
				jmsHelper.invokeJMS(message);
			}
			catch (Throwable e) {
				logger.severe("Cannot contact send to jms: " + e.getMessage());
				e.printStackTrace();
				event.setType(BaseEvent.TYPE_LOYALTY_CHANGED_FAILED);
				return ReactiveStreams.of(jsonb.toJson(event));
			}
			logger.info("Message sent to jms");
			loyaltyChangeEvent.setType(BaseEvent.TYPE_LOYALTY_CHANGE_NOTIFIED);
			return ReactiveStreams.of(jsonb.toJson(loyaltyChangeEvent));
		}
		else {
			logger.info("Not interested in event: " + type);
			return ReactiveStreams.empty();
		}
	}
}
