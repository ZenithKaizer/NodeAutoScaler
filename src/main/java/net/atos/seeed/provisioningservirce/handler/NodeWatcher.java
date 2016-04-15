package net.atos.seeed.provisioningservirce.handler;

import net.atos.seeed.provisioningservirce.model.ScaleEvent;
import net.atos.seeed.provisioningservirce.model.ScaleAction;
import net.atos.seeed.provisioningservirce.model.ScaleCause;

import net.atos.seeed.provisioningservirce.model.PodEvent;
import net.atos.seeed.provisioningservirce.model.PodAction;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

public class NodeWatcher extends UntypedActor {

  private static final Logger logger = LoggerFactory.getLogger(NodeWatcher.class);
  
  private Map<String, Integer> nodes;
  
  private ActorRef nodeAutoScaler;
  
  public NodeWatcher(ActorRef nodeAutoScaler, Map<String, Integer> nodes) {
    this.nodeAutoScaler = nodeAutoScaler;
    this.nodes = nodes;
  }
    
  public void onReceive(Object message) {
    if (message instanceof PodEvent) {
      PodEvent podEvent = (PodEvent) message;
      
      switch (podEvent.getPodAction()) {
      case POD_DELETED_FROM_NODE:
        nodes.put (podEvent.getNode(), nodes.get(podEvent.getNode()) - 1);
        if (nodes.get(podEvent.getNode()) == 0) nodeAutoScaler.tell(new ScaleEvent(ScaleAction.SHOULD_REMOVE_NODE, ScaleCause.NODE_EMPTY, podEvent.getNode()),self());
        break;
      case POD_ADDED_TO_NODE:
        if (nodes.get(podEvent.getNode()) == null)
          nodes.put(podEvent.getNode(), new Integer(1)); 
        else
          nodes.put (podEvent.getNode(), nodes.get(podEvent.getNode()) - 1);
        break;
      default:
        break;
      }      
    } 
    logger.error("{}", message.toString());
  }

}    