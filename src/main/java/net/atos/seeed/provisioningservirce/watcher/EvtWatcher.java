package net.atos.seeed.provisioningservirce.watcher;

import io.fabric8.kubernetes.api.model.Event;

import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

import net.atos.seeed.provisioningservirce.model.NasEvent;
import net.atos.seeed.provisioningservirce.queue.NasEventQueue;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class EvtWatcher extends UntypedActor implements Watcher<Event> {

  private static final Logger logger = LoggerFactory.getLogger(EvtWatcher.class);
  
  private KubernetesClient kube;
  private String namespace;
  private ActorRef nasEventHandler;
    
  public EvtWatcher(KubernetesClient kube, String namespace, ActorRef nasEventHandler){
    Map<String, String> myMap; myMap = new HashMap<String, String>(); myMap.put("source", "default-scheduler");  myMap.put("reason", "FailedScheduling,");
    this.namespace = namespace;
    this.kube = kube;
    this.nasEventHandler = nasEventHandler;
    Watch watch = kube.events().inNamespace(namespace).withFields(myMap).watch(this);
  } 
  
  @Override
  public void eventReceived(Action action, Event resource) {
    NasEvent nasEvt = null;
       
    switch (resource.getSource().getComponent()){    
    /* Evt provenant du scheduler */
    case  "default-scheduler":
      switch (resource.getReason()){
        case "FailedScheduling":
          if (resource.getMessage().contains("Node didn't have enough resource:")){
             nasEvt = new NasEvent(this.namespace, resource.getSource().getComponent(), "FailedScheduling", resource.getFirstTimestamp(), resource.getMessage());
          }
          break;
        default:
          break;       
      }
       
      break;
    
    /* Evt provenant de l'admission controller */  
    case "replicaset-controller":
      switch (resource.getReason()){
        case "FailedCreate":
          if (resource.getMessage().contains("Exceeded quota")){
             nasEvt = new NasEvent(this.namespace, resource.getSource().getComponent(), "ExceededQuota", resource.getFirstTimestamp(), resource.getMessage());
          }
          break;
        default:
          break;       
      }
      break;  
   
    default:
      break;
    }
    
    /* Ajout de l'evt K8S dans la file des evt a traiter */
    if (nasEvt != null) nasEventHandler.tell(nasEvt, self());
    
    /* Journalisation des Evts recus */    
    logger.info("{}: {}", action, resource.getSource().getComponent() + " - " + resource.getReason()+ " - " + resource.getMessage());
  }
            
  @Override
  public void onClose(KubernetesClientException e) {
    if (e != null) {
      logger.error(e.getMessage(), e);
    }
  }
  
  @Override
  public void onReceive(Object message) {
  
  }
}