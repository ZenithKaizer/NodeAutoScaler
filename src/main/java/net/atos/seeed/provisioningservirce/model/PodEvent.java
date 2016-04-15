package net.atos.seeed.provisioningservirce.model;

public class PodEvent {
  private String namespace;
  private String node;
  private PodAction podAction;
  
  public PodEvent(String namespace, String node, PodAction podAction){
    this.namespace = namespace;
    this.node = node;
    this.podAction = podAction;
  }
  
  public String getNamespace(){
    return this.namespace;
  } 
  
  public String getNode() {
    return this.node;
  } 
  
  public PodAction getPodAction() {
    return this.podAction;
  } 
  
  public String toString(){
    return this.namespace + " - " + this.podAction + " - " + this.node; //+ " - " + this.message;
  }
}