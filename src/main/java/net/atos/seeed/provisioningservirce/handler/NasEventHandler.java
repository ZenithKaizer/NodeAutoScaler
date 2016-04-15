package net.atos.seeed.provisioningservirce.handler;

import java.util.List;
import java.util.ArrayList;

import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;

import io.fabric8.kubernetes.api.model.Quantity;

import net.atos.seeed.provisioningservirce.model.NasEvent;

import akka.actor.UntypedActor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NasEventHandler extends UntypedActor {

  private static final Logger logger = LoggerFactory.getLogger(NasEventHandler.class);
  
  private ResourceQuota quota; 
  
  public NasEventHandler() {
  
  }
  
  public void handle(NasEvent event){
    /*  Ajout d'un nouveau noeud au namespace du cluster K8S */ 
    
    //quota = new ResourceQuotaBuilder().withNewMetadata().withName("default-ns-quota").endMetadata().withNewSpec().addToHard("cpu", new Quantity("10")).endSpec().build();
    //logger.info("Deleting resource quota", this.kube.resourceQuotas().inNamespace(this.namespace).delete(quota));                    
    //logger.info("Recreating resource quota", this.kube.resourceQuotas().inNamespace(this.namespace).create(quota));
    
    logger.error("{}", event.toString());
  }
  
  public void onReceive(Object message) {
    logger.error("{}", message.toString());
  }

}    