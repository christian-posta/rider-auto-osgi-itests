package org.jboss.fuse.camel.ws;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.fuse.camel.model.Order;

@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface OrderEndpoint {

  public String order(Order in);
}
