package net.atos.seeed.provisioningservirce.queue;

import java.util.List;
import java.util.ArrayList;

import net.atos.seeed.provisioningservirce.model.NasEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NasEventQueue {  
  
  private static final Logger logger = LoggerFactory.getLogger(NasEventQueue.class);
  
  private List<NasEvent> nasEvents = new ArrayList<NasEvent>();
  
  public NasEventQueue() {
  
  }
  
  public void push(NasEvent event){
    nasEvents.add(event);
    logger.error("{}", event.toString());
  }
  
  public NasEvent pop( ){
    if (nasEvents.size() != 0)
      return nasEvents.get(nasEvents.size()-1);
    else 
      return null;  
  }  
}    