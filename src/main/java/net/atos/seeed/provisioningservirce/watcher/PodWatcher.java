package net.atos.seeed.provisioningservirce.watcher;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

import net.atos.seeed.provisioningservirce.model.NasEvent;
import net.atos.seeed.provisioningservirce.queue.NasEventQueue;

public class PodWatcher implements Watcher<Pod>{

  private static final Logger logger = LoggerFactory.getLogger(PodWatcher.class);
  
  private KubernetesClient kube;
  private String namespace;
  private NasEventQueue nasEventQueue;
  
  public PodWatcher(KubernetesClient kube, String namespace, NasEventQueue nasEventQueue){
    this.namespace = namespace;
    this.kube = kube;
    this.nasEventQueue = nasEventQueue;
    Watch watch = kube.pods().inNamespace(namespace).watch(this);
  } 
   
  @Override
  public void eventReceived(Action action, Pod resource) {
    NasEvent nasEvt = null;
    switch(action) {
    case DELETED: 
      nasEvt = new NasEvent(this.namespace, "Node-"+resource.getStatus().getHostIP(), "PodDeleted", resource.getMetadata().getDeletionTimestamp(), "Pod "+resource.getMetadata().getName() + "deleted.");
      break;
    default:
      break;
    } 
     
    /* Ajout de l'evt K8S dans la file des evt a traiter */
    if (nasEvt != null) nasEventQueue.push(nasEvt);   
  }
            
  @Override
  public void onClose(KubernetesClientException e) {
    if (e != null) {
      logger.error(e.getMessage(), e);
    }
  }
}