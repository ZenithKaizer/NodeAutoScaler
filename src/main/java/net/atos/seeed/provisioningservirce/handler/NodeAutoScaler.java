package net.atos.seeed.provisioningservirce.handler;

import net.atos.seeed.provisioningservirce.model.NasEvent;

import akka.actor.UntypedActor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeAutoScaler extends UntypedActor {

  private static final Logger logger = LoggerFactory.getLogger(NodeAutoScaler.class);
  
  public NodeAutoScaler() {
  
  }
    
  public void onReceive(Object message) {
    logger.error("{}", message.toString());
  }
} 