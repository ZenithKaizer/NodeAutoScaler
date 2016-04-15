package net.atos.seeed.provisioningservirce.model;

public class NasEvent {
  public String namespace;
  public String component;
  public String reason;
  public String timeCreated;
  public String message;
  
  public NasEvent(String namespace, String component, String reason, String timeCreated, String message){
    this.namespace = namespace;
    this.component = component;
    this.reason = reason;
    this.timeCreated = timeCreated;
    this.message = message;
  } 
  
  public String toString(){
    return this.namespace + " - " + this.component + " - " + this.reason; //+ " - " + this.message;
  }
}