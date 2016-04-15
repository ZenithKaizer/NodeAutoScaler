/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package net.atos.seeed.provisioningservirce;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.pattern.Patterns.ask;
import akka.dispatch.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import net.atos.seeed.provisioningservirce.watcher.PodWatcher;
import net.atos.seeed.provisioningservirce.watcher.EvtWatcher;

import net.atos.seeed.provisioningservirce.model.ScaleEvent;
import net.atos.seeed.provisioningservirce.model.ScaleAction;
import net.atos.seeed.provisioningservirce.model.ScaleCause;

import net.atos.seeed.provisioningservirce.model.PodEvent;
import net.atos.seeed.provisioningservirce.model.PodAction;


import net.atos.seeed.provisioningservirce.queue.NasEventQueue;
import net.atos.seeed.provisioningservirce.handler.NasEventHandler;
import net.atos.seeed.provisioningservirce.handler.NodeWatcher;
import net.atos.seeed.provisioningservirce.handler.NodeAutoScaler;

/**
 * Example of the "main class". Put your bootstrap logic here.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
    
      /* 1.  Initialisation du client K8S l'API Server */ 
      try (final KubernetesClient client = new DefaultKubernetesClient()) {
    
        /*  2 . Creation du  Systeme d'acteurs 'ProvService' */ 
        final ActorSystem system = ActorSystem.create("ProvService");
        
        /*  3. Creation de l'acteur NodeAutoScaler Interface avec le cloud provider */
        final ActorRef nodeAutoScaler = system.actorOf(Props.create(NodeAutoScaler.class),"NodeAutoScaler");
        
        /*  4. Creation de l'acteur NodeWatcher charger de watcher la liberation totale ou partielle des noeuds */
        final ActorRef nodeWatcher = system.actorOf(Props.create(NodeWatcher.class, nodeAutoScaler, getNodesConfig(client,"default")),"NodeWatcher");        
        
        /*  5. Creation de failedScheduler event watcher  */         
        try (Watch failedScheduling = client.events().inNamespace("default").withField("source","default-scheduler,reason=FailedScheduling").watch(new Watcher<Event>(){
          @Override
          public void eventReceived(Action action, Event resource) {
            ask(nodeAutoScaler, new ScaleEvent(ScaleAction.SHOULD_ADD_NODE, ScaleCause.FAILED_SCHEDULING, null), 10);
          }
          @Override
          public void onClose(KubernetesClientException e) {
            if (e != null) {
              logger.error(e.getMessage(), e);             
            }
          }
        })) {
          //closeLatch.await(10, TimeUnit.SECONDS);
          // Body
          //
        } catch (KubernetesClientException e) {
          logger.error("Could not watch resources", e);
        }
        
        /*  6. Creation de failedScheduler event watcher  .withField("source","default-scheduler,reason=FailedScheduling") */         
        try (Watch failedScheduling = client.pods().inNamespace("default").watch(new Watcher<Pod>(){
          @Override
          public void eventReceived(Action action, Pod resource) {
            switch(action){               
            case DELETED:
               ask(nodeWatcher, new PodEvent("default", resource.getSpec().getNodeName(), PodAction.POD_DELETED_FROM_NODE), 10);
               break;
            case MODIFIED:
               ask(nodeWatcher, new PodEvent("default", resource.getSpec().getNodeName(), PodAction.POD_ADDED_TO_NODE), 10);   
               break;
            default:
               break;
            }                      
          }
          @Override
          public void onClose(KubernetesClientException e) {
            if (e != null) {
              logger.error(e.getMessage(), e);             
            }
          }
        })) {
          //closeLatch.await(10, TimeUnit.SECONDS);
          // Body
          //
        } catch (KubernetesClientException e) {
          logger.error("Could not watch resources", e);
        }       
        
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(e.getMessage(), e);
        Throwable[] suppressed = e.getSuppressed();
        if (suppressed != null) {
          for (Throwable t : suppressed) {
            logger.error(t.getMessage(), t);
          }
        }
      }
    }
    
    public static Map<String, Integer> getNodesConfig(KubernetesClient client, String namespace){
      Map<String, Integer> nodes = new HashMap<String, Integer>();      
      List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
      for (Pod item : pods) {
        if (nodes.get(item.getSpec().getNodeName()) == null)
          nodes.put(item.getSpec().getNodeName(), new Integer(1)); 
        else
          nodes.put (item.getSpec().getNodeName(), nodes.get(item.getSpec().getNodeName()) + 1);
        
        logger.info(" New pod found! Node {} : {} pod(s) ", item.getSpec().getNodeName(), nodes.get(item.getSpec().getNodeName()));
      }
      return nodes;
    }
}
