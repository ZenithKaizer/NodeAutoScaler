package net.atos.seeed.provisioningservirce.watcher;

import akka.actor.UntypedActor;
import akka.actor.Props;


public class Test extends UntypedActor {

  public Test() {
  
  }
  /*
   public static Props props() {
      return Props.create(new Creator<Test>() {
          private static final long serialVersionUID = 1L;
 
          @Override
          public Test create() throws Exception {
            return new Test();
          }
      });
  }*/
  
  @Override
  public void onReceive(Object msg) throws Exception {
  
  }
  
}