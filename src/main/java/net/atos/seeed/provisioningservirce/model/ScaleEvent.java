package net.atos.seeed.provisioningservirce.model;

public class ScaleEvent {
  private ScaleAction action;
  private ScaleCause cause; 
  private String nodeName;
  
  public ScaleEvent(ScaleAction action, ScaleCause cause, String nodeName){
    this.action = action;
    this.cause = cause;
    this.nodeName = nodeName;
  } 
  
  public ScaleAction getAction() {
    return this.action;
  }
  
  public ScaleCause getCause() {
    return this.cause;
  }
  
  public String getNodeName() {
    return this.nodeName;
  }
  
  public String toString(){    
    return this.nodeName == null ? this.action + " - " + this.cause : this.action + " - " + this.cause + " - " + this.nodeName ; 
  }
}